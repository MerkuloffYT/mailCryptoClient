package com.mailclient.entity;

import lombok.AllArgsConstructor;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

@AllArgsConstructor
public class AccountAuthenticator extends Authenticator
{
    private final String login;
    private final String password;

    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(login, password);
    }
}