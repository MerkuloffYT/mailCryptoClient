package com.mailclient.entity;


import javafx.scene.control.Alert;
import lombok.Data;
import lombok.SneakyThrows;

import javax.mail.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class MailSession {
    private String email;
    private String password;
    private Map<String, String> folderNames;
    private Store store;
    private Session session;
    private List<Folder> foldersList;
    private String name;
    private String host;

    public MailSession(){
        this.email = null;
    }

    @SneakyThrows
    public MailSession(String email, String password, Map<String, String> folderNames, String host) {
        this.email = email;
        this.password = password;
        this.folderNames = folderNames;
        this.host = host;

        Authenticator auth = new AccountAuthenticator(email, password);
        Properties props = loadProperties();
        session = Session.getDefaultInstance(props, auth);
        store = session.getStore("imaps");
        try {
            store.connect(props.getProperty("mail.imap.host") , email, password);
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(email + ": ошибка аутентификации");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        foldersListInit();
        for(Folder folder : foldersList) {
            Thread thread = new Thread(new Runnable() {
                @SneakyThrows
                @Override
                public void run() {
                    folder.open(Folder.READ_ONLY);
                }
            });
            thread.start();
        }
    }

    @SneakyThrows
    public Folder getFolder(String folderName) {
        if (!store.isConnected()) {
            store.connect();
            foldersListInit();
            for(Folder folder : foldersList) {
                folder.open(Folder.READ_ONLY);
            }
        }
        return foldersList.stream()
                .filter(folder -> folder.getFullName().equals(folderNames.get(folderName)))
                .findAny()
                .get();
    }

    @SneakyThrows
    private void foldersListInit() {
        foldersList = Stream.of(
                store.getFolder(folderNames.get("inbox")),
                store.getFolder(folderNames.get("sent")),
                store.getFolder(folderNames.get("trash")),
                store.getFolder(folderNames.get("drafts"))
        ).collect(Collectors.toList());
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        props.setProperty("mail.debug", "false");
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.host", "imap." + host);
        props.setProperty("mail.imap.port", "993");
        props.setProperty("mail.imaps.connectiontimeout", "5000");
        props.setProperty("mail.imaps.timeout", "5000");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.host", "smtp." + host);
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        return props;
    }
}
