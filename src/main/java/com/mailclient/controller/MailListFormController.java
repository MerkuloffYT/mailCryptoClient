package com.mailclient.controller;

import com.mailclient.component.MailListViewCell;
import com.mailclient.entity.Mail;
import com.mailclient.entity.MailSession;
import com.mailclient.service.MailService;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import lombok.SneakyThrows;

import javax.mail.Folder;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MailListFormController implements Initializable {

    @FXML private ListView<Mail> mailsListView;
    @FXML private AnchorPane mainPane;
    @FXML private ImageView nextPageButton;
    @FXML private ImageView prevPageButton;
    @FXML private TextField pageTextField;

    private ObservableList<Mail> mailObservableList;

    Folder folder;
    Integer currentPage;
    Integer maxPage;

    MailService mailService;
    MailSession mailSession;
    AnchorPane majorPane;

    public MailListFormController(Folder folder, int page, MailService mailService, MailSession mailSession, AnchorPane majorPane) {
        this.folder = folder;
        this.currentPage = page;
        this.mailService = mailService;
        this.mailSession = mailSession;
        this.majorPane = majorPane;
    }

    public MailListFormController(Folder folder, MailService mailService, MailSession mailSession, AnchorPane majorPane) {
        this(folder, 1, mailService, mailSession, majorPane);
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mailObservableList = FXCollections.observableArrayList();
        maxPage = (int) Math.ceil((double) folder.getMessageCount() / MailService.COUNT_MAILS_ON_PAGE);
        mailsListView.setItems(mailObservableList);
        mailsListView.setCellFactory(mailListView -> new MailListViewCell());
        loadPage();

        mailsListView.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @SneakyThrows
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                handleOnMailListViewClicked();
            }
        });

        nextPageButton.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                handleOnNextPageButtonClicked();
            }
        });

        prevPageButton.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                handleOnPrevPageButtonClicked();
            }
        });

        pageTextField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldPropertyValue, Boolean newPropertyValue) {
                if (!newPropertyValue) {
                    currentPage = Integer.parseInt(pageTextField.getText());
                    loadPage();
                }

            }
        });
    }

    @SneakyThrows
    private void handleOnMailListViewClicked() {
        Mail mail = mailsListView.getSelectionModel().getSelectedItem();
        mail.setContent(mailService.getMailContent(folder, mail.getNumber()));
        mail.setAttachments(mailService.getAttachmentFiles(folder, mail.getNumber()));

        if (folder.getName().equals("Черновики")) {
            MailWritingFormController mailWritingFormController = new MailWritingFormController(
                    mailService.getReplyTo(folder, mail.getNumber()),
                    mail.getSubject(),
                    mail.getContent(),
                    majorPane,
                    mailService,
                    mail.getAttachments()
            );
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_writing_form.fxml"));
            loader.setController(mailWritingFormController);
            majorPane.getChildren().remove(mainPane);
            majorPane.getChildren().add(loader.load());
        } else {
            MailReadingFormController mailReadingFormController = new MailReadingFormController(mail, mainPane);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_reading_form.fxml"));
            loader.setController(mailReadingFormController);
            mailsListView.setVisible(false);
            mainPane.getChildren().add(loader.load());
        }
    }

    private void handleOnPrevPageButtonClicked() {
        currentPage--;
        pageTextField.setText(currentPage.toString());
        loadPage();
    }

    private void handleOnNextPageButtonClicked() {
        currentPage++;
        pageTextField.setText(currentPage.toString());
        loadPage();
    }

    @SneakyThrows
    private void loadPage() {
        prevPageButton.setVisible(currentPage != 1);
        nextPageButton.setVisible(!currentPage.equals(maxPage));

        List<Mail> mails = mailService.getMails(folder, currentPage);
        mailObservableList.clear();
        mailObservableList.addAll(mails);
    }
}
