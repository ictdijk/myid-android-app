package in.yagnyam.myid;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import in.yagnyam.myid.nodeApi.NodeApi;
import in.yagnyam.myid.nodeApi.model.EntityNode;
import in.yagnyam.myid.utils.PemUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    static {
        StaticRegistrar.register();
    }

    public static Map<String, String> getClaims(HttpServletRequest request) throws InvalidJwtException, MalformedClaimException {
        String authorization = request.getHeader("X-MijD");
        Map<String, String> results = new HashMap<>();
        if (authorization == null) {
            return results;
        }
        log.info("X-MijD => {}", authorization);
        results.put("X-MijD", authorization.substring(0, Math.min(authorization.length(), 25)) + "...");

        JwtConsumer firstPassJwtConsumer = new JwtConsumerBuilder()
                .setSkipAllValidators()
                .setDisableRequireSignature()
                .setSkipSignatureVerification()
                .build();

        //The first JwtConsumer is basically just used to parse the JWT into a JwtContext object.
        JwtContext jwtContext = firstPassJwtConsumer.process(authorization);
        String issuer = jwtContext.getJwtClaims().getIssuer();
        results.put("Issuer", issuer);
        results.put("Subject", jwtContext.getJwtClaims().getSubject());
        results.put("Issued At", jwtContext.getJwtClaims().getIssuedAt().toString());
        results.put("Expiration Time", jwtContext.getJwtClaims().getExpirationTime().toString());
        results.put("Not before", jwtContext.getJwtClaims().getNotBefore().toString());
        results.put("Audience", Arrays.toString(jwtContext.getJwtClaims().getAudience().toArray()));
        results.put("Name", jwtContext.getJwtClaims().getStringClaimValue("name"));
        results.put("BSN", jwtContext.getJwtClaims().getStringClaimValue("bsn"));
        results.put("DOB", jwtContext.getJwtClaims().getStringClaimValue("dob"));
        try {
            EntityNode node = getNode(issuer);
            PublicKey verificationKey = PemUtils.decodePublicKey(node.getVerificationKey());

            JwtConsumer secondPassJwtConsumer = new JwtConsumerBuilder()
                    .setExpectedIssuer(issuer)
                    .setVerificationKey(verificationKey)
                    .setSkipDefaultAudienceValidation()
                    //.setRequireExpirationTime()
                    //.setAllowedClockSkewInSeconds(30)
                    //.setRequireSubject()
                    .build();

            // Finally using the second JwtConsumer to actually validate the JWT. This operates on
            // the JwtContext from the first processing pass, which avoids redundant parsing/processing.
            secondPassJwtConsumer.processContext(jwtContext);
        } catch (Throwable t) {
            log.error("Error while verifying Token", t);
            results.put("error", t.getMessage());
        }
        return results;
    }

    private static EntityNode getNode(String path) throws IOException {
        log.debug("getNode({})", path);
        NodeApi nodeApi = AppConstants.getNodeApi();
        EntityNode node = nodeApi.fetchNode(path).execute();
        log.info("getNode({}) => {}", path, node);
        return node;
    }
}
