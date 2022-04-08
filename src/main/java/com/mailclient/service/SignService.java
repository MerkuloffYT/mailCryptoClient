package com.mailclient.service;

import com.mailclient.crypto.CryptoUtils;
import lombok.SneakyThrows;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SignService {

    /**
     * Формирование цифровой подписи
     *
     * @param htmlContent текст письма
     * @param attachments вложения
     */
    @SneakyThrows
    public static void createSign(String htmlContent, List<DataSource> attachments) {
        // Генерируем открытый и закрытый ключи, алгоритм = DSA, keySize = 1024
        KeyPair keyPair = CryptoUtils.generateKeyPair("DSA", 1024);
        // Создание цифровой подписи
        Signature signature = Signature.getInstance("SHA1withDSA");
        // Инициализация подписи закрытым ключом
        signature.initSign(keyPair.getPrivate(), new SecureRandom());
        // Формирование цифровой подписи сообщения с закрытым ключом
        signature.update(generateHash(htmlContent, attachments));

        byte[] sign = Base64.getEncoder().encode(signature.sign());
        byte[] publicKeyBytes = Base64.getEncoder().encode(keyPair.getPublic().getEncoded());
        byte[] data = new byte[sign.length + publicKeyBytes.length];
        System.arraycopy(publicKeyBytes, 0, data, 0, publicKeyBytes.length);
        System.arraycopy(sign, 0, data, publicKeyBytes.length, sign.length);
        DataSource signDataSource = new ByteArrayDataSource(data, "text/plain");
        ((ByteArrayDataSource) signDataSource).setName("sign");
        attachments.add(signDataSource);
    }

    /**
     * Проверка цифровой подписи:
     * 1. Формируется цифровая подпись сообщения с использованием открытого ключа.
     * 2. Из файла sign извлекается цифровая подпись, созданная закрытым ключом
     * 3. Выполняется проверка методом verify().
     *
     * @param htmlContent текст письма
     * @param attachments вложения
     * @return признак валидности
     */
    @SneakyThrows
    public static boolean verifySign(String htmlContent, List<DataSource> attachments) {
        // находим среди вложений файл с открытым ключом подписи
        Optional<DataSource> signDataSource = CryptoUtils.getFileFromAttachments("sign", attachments);
        // удаляем служебные файлы из вложений
        signDataSource.ifPresent(attachments::remove);

        InputStream is = signDataSource.get().getInputStream();
        byte[] pubKey = new byte[592];
        is.read(pubKey);
        byte[] sign = new byte[is.available()];
        is.read(sign);


        Signature signature = Signature.getInstance("SHA1withDSA");
        KeyFactory rsaKf = KeyFactory.getInstance("DSA");
        PublicKey publicKey = rsaKf.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pubKey)));
        // Инициализация цифровой подписи открытым ключом
        signature.initVerify(publicKey);
        // Формирование цифровой подпись сообщения с открытым ключом
        signature.update(generateHash(htmlContent, attachments));
        // Проверка цифровой подписи
        return signature.verify(Base64.getDecoder().decode(sign));
    }

    @SneakyThrows
    private static byte[] generateHash(String content, List<DataSource> attachments) {
        StringBuilder signContent = new StringBuilder(String.valueOf(content));
        for(DataSource attachment : attachments) {
            String attCont = new BufferedReader(
                    new InputStreamReader(attachment.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));
            signContent.append(attCont);
        }
        MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
        return messageDigest.digest(Base64.getEncoder().encode(signContent.toString().getBytes()));
    }
}
