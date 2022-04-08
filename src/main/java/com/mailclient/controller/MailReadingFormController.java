package com.mailclient.controller;

import com.mailclient.entity.Mail;
import com.mailclient.service.CryptoService;
import com.mailclient.service.SignService;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.util.Pair;
import lombok.SneakyThrows;

import javax.activation.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class MailReadingFormController implements Initializable {

    @FXML private Circle letterCircle;
    @FXML private Label circleLetter;
    @FXML private Label fromNameLabel;
    @FXML private Label fromEmailLabel;
    @FXML private Label sentDateLabel;
    @FXML private Label subjectLabel;
    @FXML private Label messageLabel;
    @FXML private Label isDecryptedLabel;
    @FXML private WebView contentWebView;
    @FXML private FlowPane attachmentsPane;
    @FXML private ImageView backButton;

    private final Mail mail;
    private final AnchorPane mainPane;
    private boolean isSigned;
    private boolean isVerifiedSign;
    private boolean isDecrypted;

    public MailReadingFormController(Mail mail, AnchorPane mainPane) {
        this.mail = mail;
        this.mainPane = mainPane;
        this.isSigned = false;
        for(DataSource attachment : mail.getAttachments()) {
            if (attachment.getName() != null) {
                if(attachment.getName().equals("publickey")) {
                    try {
                        Pair<String, List<DataSource>> pair = CryptoService.decrypt(mail.getContent(), mail.getAttachments(), this.mail.getFromEmail());
                        this.mail.setContent(pair.getKey());
                        this.mail.setAttachments(pair.getValue());
                        isDecrypted = true;
                        System.out.println("Mail was decrypted");
                    } catch (Exception e) {
                        System.err.println("Mail decryption ERROR!");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        for(DataSource attachment : mail.getAttachments()) {
            if (attachment.getName() != null) {
                if (attachment.getName().equals("sign")) {
                    try {
                        this.isSigned = true;
                        this.isVerifiedSign = SignService.verifySign(mail.getContent(), mail.getAttachments());
                        System.out.println("Verification of a signed object: " + ((isVerifiedSign) ? "success" : "failure"));
                    } catch (Exception e) {
                        System.err.println("Verification of a signed object ERROR!");
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Random rand = new Random();
        letterCircle.setFill(Color.rgb(rand.nextInt(255) + 1, rand.nextInt(255) + 1, rand.nextInt(255) + 1));
        if(mail.getFromName() != null && mail.getFromName().length() != 0) {
            circleLetter.setText(String.valueOf(mail.getFromName().toUpperCase().charAt(0)));
        }
        else {
            circleLetter.setText(String.valueOf(mail.getFromEmail().toUpperCase().charAt(0)));
        }
        fromNameLabel.setText(mail.getFromName());
        subjectLabel.setText(mail.getSubject());
        fromEmailLabel.setText(mail.getFromEmail());
        contentWebView.getEngine().loadContent(mail.getContent());
        if(isSigned) {
            if(isVerifiedSign) {
                messageLabel.setText("Письмо подписано ЭЦП. ЭЦП валидна.");
            } else {
                messageLabel.setText("Письмо подписано ЭЦП. ЭЦП не валидна.");
            }
        }
        if (isDecrypted) isDecryptedLabel.setText("RSA шифрование");
        for(DataSource dataSource : mail.getAttachments()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/attachment_reading_cell.fxml"));
            AttachmentReadingCellController attachmentReadingCellController = new AttachmentReadingCellController(dataSource);
            loader.setController(attachmentReadingCellController);
            attachmentsPane.getChildren().add(loader.load());
        }

        backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mainPane.getChildren().remove(contentWebView.getParent());
                mainPane.getChildren().get(0).setVisible(true);
                mainPane.getChildren().get(0).requestFocus();
            }
        });
    }
}
