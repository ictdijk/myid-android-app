package in.yagnyam.myid;

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
        Security.addProvider(new BouncyCastleProvider());
    }

}
