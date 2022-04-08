package com.mailclient.controller;

import com.mailclient.crypto.RSACrypt;
import com.mailclient.service.AccountService;
import com.mailclient.service.CryptoService;
import com.mailclient.service.MailService;
import com.mailclient.service.SignService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.SneakyThrows;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MailWritingFormController implements Initializable {

    @FXML private HTMLEditor contentEditor;
    @FXML private TextField toEmailTextField;
    @FXML private TextField subjectTextField;
    @FXML private Label errorLabel;
    @FXML private Button sendButton;
    @FXML private ImageView backButton;
    @FXML private ImageView addAttachmentButton;
    @FXML private ListView listView;
    @FXML private FlowPane attachmentPane;
    @FXML private CheckBox isNeedCryptoCheckBox;
    @FXML private CheckBox signCheckBox;

    private final AnchorPane mainPane;
    private final MailService mailService;
    private final List<DataSource> attachments;
    private String toEmail;
    private String subject;
    private String content;

    public MailWritingFormController(AnchorPane mainPane, MailService mailService) {
        this.mainPane = mainPane;
        this.mailService = mailService;
        attachments = new ArrayList<>();
    }

    public MailWritingFormController(String toEmail, String subject, String content, AnchorPane mainPane, MailService mailService, List<DataSource> attachments) {
        this.toEmail = toEmail;
        this.subject = subject;
        this.content = content;
        this.attachments = attachments;
        this.mainPane = mainPane;
        this.mailService = mailService;
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if(toEmail != null) {
            toEmailTextField.setText(toEmail);
            subjectTextField.setText(subject);
            contentEditor.setHtmlText(content);
            for(DataSource dataSource : attachments) {
                AttachmentWritingCellController attachmentWritingCellController = new AttachmentWritingCellController(
                        dataSource.getName(), attachments, attachmentPane, addAttachmentButton
                );
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/attachment_writing_cell.fxml"));
                loader.setController(attachmentWritingCellController);
                attachmentPane.getChildren().add(loader.load());
            }
        }
        backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) { handleOnBackButtonClick(); }
        });
        sendButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleOnSendEmailButtonClick();
            }
        });
        addAttachmentButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleOnAddAttachmentButtonClick();
            }
        });
    }

    public void handleOnBackButtonClick() {
        mainPane.getChildren().remove(backButton.getParent());
        mainPane.getChildren().get(0).setVisible(true);
    }

    public void handleOnSendEmailButtonClick() {
        String toEmail = toEmailTextField.getText();
        String subject = subjectTextField.getText();
        String htmlContent = contentEditor.getHtmlText();
        String errorMessage = null;
        boolean isSuccess = false;
        try {
            if(!toEmail.matches("^.*@(mail[.]ru|gmail[.]com|yandex[.]ru)$")) {
                throw new MessagingException("Некорректный email адрес");
            }
            if(signCheckBox.isSelected()) {
                SignService.createSign(htmlContent, attachments);
            }
            if(isNeedCryptoCheckBox.isSelected()) {
                String publicRsaKey = RSACrypt.generateRsaPublicKey(AccountService.getCurrentMailSession().getEmail(), toEmail);
                Pair<String, List<DataSource>> pair = CryptoService.encrypt(htmlContent, attachments, publicRsaKey);
                System.out.println(AccountService.getCurrentMailSession().getEmail() + "mail was encrypt by RSA algorithm, publicKey: " + publicRsaKey);
                mailService.sendEmail(toEmail, subject, pair.getKey(), pair.getValue());
            } else {
                mailService.sendEmail(toEmail, subject, htmlContent, attachments);
            }
            isSuccess = true;
            System.out.println(AccountService.getCurrentMailSession().getEmail() + ": mail was sent to " + toEmail + ": success");
        } catch (Exception e) {
            errorMessage = e.getMessage();
            System.out.println(AccountService.getCurrentMailSession().getEmail() + ": mail was sent to " + toEmail + ": fail");
        }

        Alert alert = new Alert(isSuccess ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle("Отправка письма");
        alert.setHeaderText(isSuccess ? "Письмо отправлено!" : "Ошибка отправки.");
        alert.setContentText(errorMessage);
        alert.showAndWait();
        if(isSuccess) handleOnBackButtonClick();
    }

    @SneakyThrows
    public void handleOnAddAttachmentButtonClick() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        attachments.add(new FileDataSource(file));
        AttachmentWritingCellController attachmentWritingCellController = new AttachmentWritingCellController(
                file.getName(), attachments, attachmentPane, addAttachmentButton
        );
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/attachment_writing_cell.fxml"));
        loader.setController(attachmentWritingCellController);
        attachmentPane.getChildren().add(loader.load());
    }
}
