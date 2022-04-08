package com.mailclient;

import javafx.application.Application;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class MailCryptoClientApplication {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        Application.launch(JavaFxApplication.class, args);
    }
}
