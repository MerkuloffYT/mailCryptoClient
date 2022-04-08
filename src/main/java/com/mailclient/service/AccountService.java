package com.mailclient.service;

import com.mailclient.entity.MailSession;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AccountService {
    /**
     * Пулл всех сессий приложения. key = email, value = MailSession
     */
    private static final Map<String, MailSession> mailSessions = new HashMap<>();

    /**
     * Текущая выбранная сессия
     */
    public static MailSession currentMailSession;

    /**
     * @return карта всех сессий приложения
     */
    public static Map<String, MailSession> getMailSessions() { return mailSessions; }

    /**
     * Возвращает MailSession из пула по email
     * @param key email
     * @return MailSession
     */
    public static MailSession getMailSession(String key) { return mailSessions.get(key); }

    /**
     * Добавляет сессию для email
     * @param key email
     * @param value сессия
     */
    public static void putMailSession(String key, MailSession value) {
        System.out.println(String.format(">> MailSession by key %s added", key));
        mailSessions.put(key, value);
    }

    /**
     * @return возвращает текущую сессию
     */
    public static MailSession getCurrentMailSession() {
        return currentMailSession;
    }

    /**
     * Устанавливает сессию как текущую
     * @param newCurrentMailSession новая сессия
     */
    public static void setCurrentMailSession(MailSession newCurrentMailSession) {
        System.out.println(String.format(
                ">> MailSession changed from %s to %s",
                currentMailSession != null ? currentMailSession.getEmail() : null,
                newCurrentMailSession.getEmail())
        );
        currentMailSession = newCurrentMailSession;
    }

    /**
     * @return список всех аккаунтов (email)
     */
    @SneakyThrows
    public static List<String> getAccounts() {
        return new ArrayList<>(mailSessions.keySet());
    }

    /**
     * Удаляет аккаунт по email
     * @param email
     */
    @SneakyThrows
    public static void deleteAccount(String email) {
        System.out.println(String.format(">> MailSession by key %s removed", email));
        mailSessions.remove(email);
    }
}
