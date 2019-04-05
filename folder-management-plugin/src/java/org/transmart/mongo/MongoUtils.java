package org.transmart.mongo;

import org.apache.commons.net.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MongoUtils {
    private static final String ALGORITHM = "SHA-256";

    public static String hash(String clear) throws NoSuchAlgorithmException {
        byte[] bin = MessageDigest.getInstance(ALGORITHM).digest(clear.getBytes());
        return Base64.encodeBase64String(bin).replace("\n", "");
    }
}
