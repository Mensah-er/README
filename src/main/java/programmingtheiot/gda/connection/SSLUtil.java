package programmingtheiot.gda.connection;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * SSL Utility to create SSLSocketFactory from CA and client certificates.
 */
public class SSLUtil {

    /**
     * Create an SSLSocketFactory using provided truststore and keystore.
     *
     * @param trustStorePath  Path to CA truststore (JKS or PKCS12)
     * @param trustStorePassword Password for truststore
     * @param keyStorePath    Path to client keystore (JKS or PKCS12) - optional, can be null
     * @param keyStorePassword Password for keystore - optional, can be null
     * @return SSLSocketFactory
     * @throws Exception
     */
    public static SSLSocketFactory getSocketFactory(
            String trustStorePath, String trustStorePassword,
            String keyStorePath, String keyStorePassword) throws Exception {

        // Load TrustStore (for server verification)
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream tsFile = new FileInputStream(trustStorePath)) {
            trustStore.load(tsFile, trustStorePassword.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyManagerFactory kmf = null;
        if (keyStorePath != null && !keyStorePath.isEmpty()) {
            // Load KeyStore (for client certificate)
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream ksFile = new FileInputStream(keyStorePath)) {
                keyStore.load(ksFile, keyStorePassword.toCharArray());
            }
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePassword.toCharArray());
        }

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf != null ? kmf.getKeyManagers() : null, tmf.getTrustManagers(), null);

        return ctx.getSocketFactory();
    }
}
