package com.mailclient.service;

import com.mailclient.entity.Mail;
import com.mailclient.entity.MailSession;
import lombok.SneakyThrows;
import org.apache.commons.mail.util.MimeMessageParser;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MailService {

    public static final int COUNT_MAILS_ON_PAGE = 11;

    @SneakyThrows
    public List<Mail> getMails(Folder folder, int page) {

        List<Mail> mails = new ArrayList<>();
        int messageCount = folder.getMessageCount();
        int messageStart = (page - 1) * COUNT_MAILS_ON_PAGE + 1;
        int messagesEnd = Math.min((page * COUNT_MAILS_ON_PAGE), messageCount);
        for (int i = messageCount - messageStart + 1; i >= messageCount - messagesEnd + 1; i--) {
            Mail mail = getMailFromMessage(folder.getMessage(i));
            mail.setNumber(i);
            mails.add(mail);
        }
        return mails;
    }

    @SneakyThrows
    private Mail getMailFromMessage(Message message) {
        Mail mail = new Mail();
        Address[] from = message.getFrom();
        mail.setFromName(((InternetAddress) from[0]).getPersonal());
        mail.setFromEmail(((InternetAddress) from[0]).getAddress());
        mail.setSubject(getMessageSubject(message));
        mail.setDate(message.getSentDate());
        return mail;
    }

    @SneakyThrows
    private String getMessageSubject(Message message){
        if (message.getSubject() != null) {
            return MimeUtility.decodeText(message.getSubject());
        }
        return "";
    }

    @SneakyThrows
    public String getMailContent(Folder folder, Integer mailNumber) {
        MimeMessage message = (MimeMessage) folder.getMessage(mailNumber);
        MimeMessageParser parser = new MimeMessageParser(message).parse();
        return parser.getHtmlContent();
    }

    @SneakyThrows
    public List<DataSource> getAttachmentFiles(Folder folder, Integer mailNumber) {
        MimeMessage message = (MimeMessage) folder.getMessage(mailNumber);
        MimeMessageParser parser = new MimeMessageParser(message).parse();
        return parser.getAttachmentList();
    }

    @SneakyThrows
    public String getReplyTo(Folder folder, Integer mailNumber) {
        MimeMessage message = (MimeMessage) folder.getMessage(mailNumber);
        return ((InternetAddress) message.getReplyTo()[0]).getAddress();
    }

    public void sendEmail(
            String toEmail,
            String subject,
            String htmlContent,
            List<DataSource> dataSources
    ) throws UnsupportedEncodingException, MessagingException {
        try {
            MailSession currentSession = AccountService.getCurrentMailSession();

            Message message = new MimeMessage(currentSession.getSession());
            message.setFrom(new InternetAddress(currentSession.getEmail(), currentSession.getEmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);

            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlContent, "text/html");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);

            for(DataSource dataSource : dataSources) {
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(dataSource));
                messageBodyPart.setFileName(dataSource.getName());
                multipart.addBodyPart(messageBodyPart);
            }
            message.setContent(multipart);
            Transport.send(message, currentSession.getEmail(), currentSession.getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
