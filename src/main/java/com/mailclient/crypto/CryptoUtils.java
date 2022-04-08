package com.mailclient.crypto;

import lombok.SneakyThrows;

import javax.activation.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Optional;

public class CryptoUtils {

    /**
     * Генерация закрытого и открытого ключей
     * @return KeyPair
     */
    @SneakyThrows
    public static KeyPair generateKeyPair(String algorithm, int keySize) {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(algorithm);
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    /**
     * @param fileName название файла
     * @param attachments список вложений
     * @return вложение Optional<DataSource>
     */
    @SneakyThrows
    public static Optional<DataSource> getFileFromAttachments(String fileName, List<DataSource> attachments) {
        return attachments.stream()
                .filter(attachment -> attachment.getName().equals(fileName))
                .findAny();
    }

    /**
     * Удаляем из списка вложений служебный файлы подписи и шифрования
     * @param attachments список вложений
     * @return отфильтрованный список вложений
     */
    public static List<DataSource> getAttachmentsWithoutServiceFile(List<DataSource> attachments) {
        // находим среди вложений файл с открытым ключом шифрования
        Optional<DataSource> publicKeyDataSource = CryptoUtils.getFileFromAttachments("publickey", attachments);
        publicKeyDataSource.ifPresent(attachments::remove);
        // Проверяем наличие файла подписи
        Optional<DataSource> signDataSource = CryptoUtils.getFileFromAttachments("sign", attachments);
        signDataSource.ifPresent(attachments::remove);

        return attachments;
    }
}
