package application;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class Encrypt {

    private static final String msg =
            "this message is 1088 bytes long this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....this is a really long message....";

    public static void main(String... args) {
        newDemo(msg);
    }

    public static void newDemo(String message) {
        System.out.println("msg: " + message);

        KeyPairGenerator generator;
        try {
            generator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        generator.initialize(4096);
        KeyPair alice = generator.generateKeyPair();
        KeyPair bob = generator.generateKeyPair();

        SecretKey aesKey = generateKey(256);
        System.out.println("aesKey: " + toBase64(aesKey.getEncoded()));

        byte[] encData = aesEncrypt(message, aesKey);
        System.out.println("encData: " + toBase64(encData));

        byte[] encKey = rsaEncrypt(aesKey.getEncoded(), bob.getPublic());
        System.out.println("encKey: " + toBase64(encKey));

        byte[] decKey = rsaDecrypt(encKey, bob.getPrivate());
        System.out.println("decKec: " + toBase64(decKey));

        SecretKey aesDec = new SecretKeySpec(decKey, "AES");
        System.out.println("aesDec: " + toBase64(aesDec.getEncoded()));

        String decMsg = aesDecrypt(encData, aesDec);
        System.out.println("decMsg: " + decMsg);
    }

    public static String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] rsaEncrypt(byte[] data, PublicKey key) {
        Cipher encryptCipher;
        try {
            encryptCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            encryptCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            return encryptCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] rsaDecrypt(byte[] data, PrivateKey key) {
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("RSA");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        try {
            return decryptCipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] aesEncrypt(String message, SecretKey key) {
        Cipher cipher;
        byte[] encryptedData;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedData = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return encryptedData;
    }

    public static String aesDecrypt(byte[] data, SecretKey key) {
        Cipher cipher;
        String decryptedData;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedData = new String(cipher.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return decryptedData;
    }

    public static SecretKey generateKey(int n) {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }
}