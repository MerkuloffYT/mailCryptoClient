package com.mailclient.crypto;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.Base64;

public class DESCrypt {
    private final Cipher encCipher;
    private final Cipher descCipher;

    @SneakyThrows
    public DESCrypt(SecretKey key) {
        encCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        descCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        encCipher.init(Cipher.ENCRYPT_MODE, key);
        descCipher.init(Cipher.DECRYPT_MODE, key);
    }

    @SneakyThrows
    public String encrypt(String input) {
        return new String(Base64.getEncoder().encode(encCipher.doFinal(input.getBytes())));
    }

    @SneakyThrows
    public String encrypt(byte[] input) {
        return new String(Base64.getEncoder().encode(encCipher.doFinal(input)));
    }

    @SneakyThrows
    public String decrypt(String input) {
        byte[] inputBytes = Base64.getDecoder().decode(input.getBytes());
        return new String(descCipher.doFinal(inputBytes));
    }

    @SneakyThrows
    public String decrypt(byte[] input) {
        return new String(descCipher.doFinal(input));
    }
}
