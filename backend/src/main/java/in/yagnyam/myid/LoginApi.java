package in.yagnyam.myid;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.BadRequestException;
import com.google.api.server.spi.response.ForbiddenException;
import com.googlecode.objectify.Key;

import in.yagnyam.myid.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Api(
        name = "loginApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "myid.yagnyam.in",
                ownerName = "myid.yagnyam.in",
                packagePath = ""
        )
)
@Slf4j
public class LoginApi {

    static {
        StaticRegistrar.register();
    }

    @ApiMethod(name = "login", httpMethod = ApiMethod.HttpMethod.POST)
    public void login(LoginEntity loginEntity) throws BadRequestException, ForbiddenException {
        if (loginEntity == null || StringUtils.isEmpty(loginEntity.getSession()) || StringUtils.isEmpty(loginEntity.getAuthToken())) {
            log.error("Invalid input: {}", loginEntity);
            throw new BadRequestException("Invalid Input");
        }
        ofy().save().entity(loginEntity).now();
        log.info("login {}", loginEntity);
    }

    @ApiMethod(name = "getAuthToken", httpMethod = ApiMethod.HttpMethod.GET)
    public LoginEntity getAuthToken(@Named("session") String session) throws BadRequestException, ForbiddenException {
        log.debug("getAuthToken {}", session);
        LoginEntity ret = ofy().load().key(Key.create(LoginEntity.class, session)).now();
        log.info("getAuthToken {} => {}", session, ret);
        return ret;
    }

}
