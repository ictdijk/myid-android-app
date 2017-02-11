package in.yagnyam.myid;

import com.googlecode.objectify.ObjectifyService;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class StaticRegistrar {

    private static Boolean done = false;

    public static void register() {
        synchronized (done) {
            if (done) {
                return;
            }
            done = true;
        }
        ObjectifyService.register(LoginEntity.class);
        Security.addProvider(new BouncyCastleProvider());
    }

}
