package com.mailclient.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.activation.DataSource;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AttachmentWritingCellController implements Initializable {
    @FXML private Label filenameLabel;
    @FXML private ImageView deleteButton;

    private String filename;
    private List<DataSource> attachments;
    private Pane pane;
    private ImageView addAttachmentButton;

    public AttachmentWritingCellController(String filename, List<DataSource> attachments, Pane pane, ImageView addAttachmentButton) {
        this.filename = filename;
        this.attachments = attachments;
        this.pane = pane;
        this.addAttachmentButton = addAttachmentButton;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filenameLabel.setText(filename);

        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                attachments.remove(
                        attachments.stream()
                        .filter(att -> att.getName().equals(filename))
                        .findAny()
                        .get()
                );
                pane.getChildren().remove(filenameLabel.getParent());
            }
        });
    }
}
