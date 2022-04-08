package com.mailclient.service;

import com.mailclient.crypto.CryptoUtils;
import com.mailclient.crypto.DESCrypt;
import com.mailclient.crypto.RSACrypt;
import com.mailclient.entity.MailSession;
import javafx.util.Pair;
import lombok.SneakyThrows;

import javax.activation.DataSource;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CryptoService {
    @SneakyThrows
    public static Pair<String, List<DataSource>> encrypt(String content, List<DataSource> attachments, String rsaPubKeyString) {
        List<DataSource> encryptedAttachments = new ArrayList<>();
        String base64content = Base64.getEncoder().encodeToString(content.getBytes());

        SecretKey secretKey = KeyGenerator.getInstance("DES").generateKey();
        DESCrypt desCrypt = new DESCrypt(secretKey);
        String encryptedContent = desCrypt.encrypt(base64content);

        for(DataSource attachment : attachments) {
            String attachmentContent = new BufferedReader(
                     new InputStreamReader(attachment.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));
            String encoded = Base64.getEncoder().encodeToString(attachmentContent.getBytes());
            String encryptedAttachmentContent = desCrypt.encrypt(encoded);
            DataSource encryptedAttachment = new ByteArrayDataSource(encryptedAttachmentContent.getBytes(), "text/html");
            ((ByteArrayDataSource) encryptedAttachment).setName(attachment.getName());
            encryptedAttachments.add(encryptedAttachment);
        }

        PublicKey rsaPublicKey = RSACrypt.getPublicKeyFromString(rsaPubKeyString);
        byte[] encryptedDesKeyString = RSACrypt.encrypt(secretKey.getEncoded(), rsaPublicKey);
        DataSource encryptedDesKeyDataSource = new ByteArrayDataSource(encryptedDesKeyString, "text/html");
        ((ByteArrayDataSource) encryptedDesKeyDataSource).setName("publickey");
        encryptedAttachments.add(encryptedDesKeyDataSource);

        return new Pair<>(encryptedContent, encryptedAttachments);
    }

    @SneakyThrows
    public static Pair<String, List<DataSource>> decrypt(String content, List<DataSource> attachments, String fromEmail) {
        MailSession currentMailSession = AccountService.getCurrentMailSession();
        File file = new File("src/main/java/com/mailclient/files/" + fromEmail + "/" + currentMailSession.getEmail());
        FileInputStream in = new FileInputStream(file);
        byte[] privateRsaKeyBytes = new byte[in.available()];
        in.read(privateRsaKeyBytes);
        PrivateKey privateKey = RSACrypt.getPrivateKeyFromByteArray(privateRsaKeyBytes);

        // находим среди вложений файл с ключом шифрования
        Optional<DataSource> publicKeyDataSource = CryptoUtils.getFileFromAttachments("publickey", attachments);
        publicKeyDataSource.ifPresent(attachments::remove);

        InputStream inputStream = publicKeyDataSource.get().getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        os.write(buffer);
        byte[] publicKey = os.toByteArray();

        byte[] base64desKey = RSACrypt.decrypt(publicKey, privateKey);
        SecretKey desKey = new SecretKeySpec(base64desKey, "DES");
        DESCrypt desCrypt = new DESCrypt(desKey);
        String decryptedContent = new String(Base64.getDecoder().decode(desCrypt.decrypt(content)));

        List<DataSource> decryptedAttachments = new ArrayList<>();
        for(DataSource attachment : attachments) {
            String attCont = new BufferedReader(
                    new InputStreamReader(attachment.getInputStream()))
                    .lines()
                    .collect(Collectors.joining("\n"));
            String decryptedAttachContent = new String(Base64.getDecoder().decode(desCrypt.decrypt(attCont)));
            DataSource decryptedAttachment = new ByteArrayDataSource(decryptedAttachContent.getBytes(), "text/html");
            ((ByteArrayDataSource) decryptedAttachment).setName(attachment.getName());
            decryptedAttachments.add(decryptedAttachment);
        }
        return new Pair<>(decryptedContent, decryptedAttachments);
    }
}