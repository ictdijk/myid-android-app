package in.yagnyam.myid;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;
import java.util.Map;

public class TokenIssuer {

    public static String issueToken(String audience, Map<String, Object> claims, String issuer, PrivateKey privateKey) throws JoseException {

        JwtClaims jwtClaims = new JwtClaims();
        jwtClaims.setIssuer(issuer);  // who creates the token and signs it
        jwtClaims.setAudience(audience); // to whom the token is intended to be sent
        jwtClaims.setExpirationTimeMinutesInTheFuture(10); // time when the token will expire (10 minutes from now)
        jwtClaims.setGeneratedJwtId(); // a unique identifier for the token
        jwtClaims.setIssuedAtToNow();  // when the token was issued/created (now)
        jwtClaims.setNotBeforeMinutesInThePast(2); // time before which the token is not yet valid (2 minutes ago)
        jwtClaims.setSubject(issuer); // the subject/principal is whom the token is about
        for (Map.Entry<String, Object> claim : claims.entrySet()) {
            jwtClaims.setClaim(claim.getKey(), claim.getValue());
        }

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(jwtClaims.toJson());
        jws.setKey(privateKey);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);

        return jws.getCompactSerialization();
    }
}
