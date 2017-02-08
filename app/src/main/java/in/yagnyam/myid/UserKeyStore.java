package in.yagnyam.myid;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

import javax.security.auth.x500.X500Principal;

public class UserKeyStore {

    private static final String TAG = "UserKeyStore";

    public static final String PROVIDER = "AndroidKeyStore";
    public static final int KEY_SIZE = 2048;
    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
    public static final String KEY_GENERATION_ALGORITHM = "RSA";

    public static final String KEY_ALIAS = "mijd";

    private static KeyStore keyStore;

    public static KeyStore getKeyStore() throws RuntimeException {
        try {
            if (keyStore == null) {
                keyStore = KeyStore.getInstance(PROVIDER);
                keyStore.load(null);
            }
            return keyStore;
            // getKeyAliases();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException e) {
            Log.e(TAG, "failed to get UserKeyStore instance", e);
            throw new RuntimeException("failed to get UserKeyStore instance", e);
        }
    }


    public static List<String> getKeyAliases() throws RuntimeException {
        try {
            List<String> keyAliases = new ArrayList<>();
            Enumeration<String> aliases = getKeyStore().aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (getKeyStore().isKeyEntry(alias)) {
                    Log.d(TAG, "found key alias - " + alias);
                    keyAliases.add(alias);
                }
            }
            return keyAliases;
        } catch (KeyStoreException e) {
            Log.e(TAG, "failed to query key aliases", e);
            throw new RuntimeException("failed to query key aliases", e);
        }
    }

    public static KeyPair getKeyPair() {
        try {
            KeyStore keyStore = getKeyStore();
            Key key = keyStore.getKey(KEY_ALIAS, null);
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                Certificate cert = keyStore.getCertificate(KEY_ALIAS);
                // Get public key
                PublicKey publicKey = cert.getPublicKey();
                // Return a key pair
                return new KeyPair(publicKey, (PrivateKey) key);
            }
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed to retrieve private Key", e);
            throw new RuntimeException("Failed to retrieve private Key", e);
        }
        throw new RuntimeException("No Key found in Keystore");
    }


    public static BigInteger nextSerialNumber() {
        return new BigInteger(64, new Random());
    }

    public static KeyPair createNewKeyPair(Context context, String name) {
        try {
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            endDate.add(Calendar.YEAR, 20);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setSerialNumber(nextSerialNumber())
                    .setAlias(KEY_ALIAS)
                    .setKeySize(KEY_SIZE)
                    .setSubject(new X500Principal("CN=" + name))
                    .setStartDate(startDate.getTime())
                    .setEndDate(endDate.getTime())
                    .setKeySize(KEY_SIZE)
                    // TODO: Enable this feature
                    // .setEncryptionRequired()
                    .build();
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM, PROVIDER);
            generator.initialize(spec, new SecureRandom());
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            Log.e(TAG, "failed to generate key pair", e);
            throw new RuntimeException("failed to generate key pair", e);
        }
    }


}
