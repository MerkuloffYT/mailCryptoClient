package com.mailclient.crypto;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSACrypt {

    @SneakyThrows
    public static String generateRsaPublicKey(String fromEmail, String toEmail) {
        KeyPair keyPair = CryptoUtils.generateKeyPair("RSA", 1024);
        String encodedPublicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        byte[] encodedPrivateKey = Base64.getEncoder().encode(keyPair.getPrivate().getEncoded());
        File file = new File("src/main/java/com/mailclient/files/" + fromEmail + "/" + toEmail);
        file.getParentFile().mkdirs();
        FileOutputStream out = new FileOutputStream(file);
        out.write(encodedPrivateKey);
        out.close();
        if (!isPublicRsaKey(encodedPublicKey)) throw new MessagingException("Ошибка генерации открытого RSA ключа");
        return encodedPublicKey;
    }

    @SneakyThrows
    public static boolean isPublicRsaKey(String rsaPublicKeyString) {
        try {
            KeyFactory rsaKf = KeyFactory.getInstance("RSA");
            rsaKf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPublicKeyString)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SneakyThrows
    public static PublicKey getPublicKeyFromString(String rsaPubKeyString) {
        KeyFactory rsaKf = KeyFactory.getInstance("RSA");
        return rsaKf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(rsaPubKeyString)));
    }

    @SneakyThrows
    public static PrivateKey getPrivateKeyFromString(String rsaPrivateKeyString) {
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKeyString)));
    }

    @SneakyThrows
    public static PrivateKey getPrivateKeyFromByteArray(byte[] rsaPrivateKeyBytes) {
        KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
        return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaPrivateKeyBytes)));
    }

    @SneakyThrows
    public static byte[] encrypt(byte[] plainText, PublicKey publicKey) {
        byte[] text = Base64.getEncoder().encode(plainText);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(text);
    }

    @SneakyThrows
    public static byte[] decrypt(byte[] encryptedText, PrivateKey privateKey) {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] rsaDecrypted = cipher.doFinal(encryptedText);
        return Base64.getDecoder().decode(rsaDecrypted);
    }
}
