package com.mailclient.configuration;

import com.mailclient.entity.MailSession;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Configuration
public class MailConfiguration {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Qualifier("new_session")
    public MailSession getNewMailSession(String email, String password) {
        String host = email.substring(email.lastIndexOf("@") + 1);
        Object[][] folders;
        if ("gmail.com".equals(host)) {
            folders = new Object[][]{
                    {"inbox", "Inbox"},
                    {"sent", "[Gmail]/Отправленные"},
                    {"drafts", "[Gmail]/Черновики"},
                    {"trash", "[Gmail]/Корзина"}
            };
        } else {
            folders = new Object[][]{
                    {"inbox", "inbox"},
                    {"sent", "Отправленные"},
                    {"drafts", "Черновики"},
                    {"trash", "Корзина"}
            };
        }
        Map<String, String> folderNames = Stream.of(folders)
                .collect(Collectors.toMap(data -> (String) data[0], data -> (String) data[1]));
        return new MailSession(email, password, folderNames, host);
    }
}
