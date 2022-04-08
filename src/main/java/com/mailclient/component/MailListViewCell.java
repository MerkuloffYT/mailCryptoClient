package com.mailclient.component;

import com.mailclient.entity.Mail;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MailListViewCell extends ListCell<Mail> {
    @FXML private Circle letterCircle;
    @FXML private Label circleLetter;
    @FXML private Label fromNameLabel;
    @FXML private Label subjectLabel;
    @FXML private Label dateLabel;
    @FXML private AnchorPane anchorPane;

    FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(Mail mail, boolean isEmpty) {
        super.updateItem(mail, isEmpty);

        if(isEmpty || mail == null){
            setText(null);
            setGraphic(null);
        } else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/mail_list_cell.fxml"));
                fxmlLoader.setController(this);
                try{
                    fxmlLoader.load();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
            letterCircle.setFill(
                    Color.rgb(
                            new Random().nextInt(255) + 1,
                            new Random().nextInt(255) + 1,
                            new Random().nextInt(255) + 1
                    )
            );
            String nameLogo;
            if(mail.getFromName() != null && mail.getFromName().length() != 0) {
                nameLogo = String.valueOf(mail.getFromName().toUpperCase().charAt(0));
            } else {
                nameLogo = String.valueOf(mail.getFromEmail().toUpperCase().charAt(0));
            }
            circleLetter.setText(nameLogo);
            fromNameLabel.setText(mail.getFromName());
            subjectLabel.setText(mail.getSubject());

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            dateLabel.setText(mail.getDate()  == null ? "" : dateFormat.format(mail.getDate()));
        }
        setText(null);
        setGraphic(anchorPane);
    }
}
