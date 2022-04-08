package com.mailclient.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import javax.activation.DataSource;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AttachmentReadingCellController implements Initializable {

    @FXML private Label filenameLabel;
    @FXML private ImageView downloadButton;

    DataSource dataSource;

    public AttachmentReadingCellController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filenameLabel.setText(dataSource.getName());

        downloadButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @SneakyThrows
            @Override
            public void handle(MouseEvent event) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File selectedDirectory = directoryChooser.showDialog(new Stage());
                if (selectedDirectory != null) {
                    File file = new File(selectedDirectory.getAbsolutePath() + "/" + dataSource.getName());
                    FileUtils.copyInputStreamToFile(dataSource.getInputStream(), file);
                }
            }
        });
    }
}
