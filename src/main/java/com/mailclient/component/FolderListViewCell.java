package com.mailclient.component;

import com.mailclient.entity.AccountFolder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class FolderListViewCell extends ListCell<AccountFolder> {
    @FXML private Label folderNameLabel;
    @FXML private ImageView folderIconImageView;
    @FXML private AnchorPane anchorPane;

    FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(AccountFolder folder, boolean isEmpty){
        super.updateItem(folder,isEmpty);

        if(isEmpty || folder == null){
            setText(null);
            setGraphic(null);
        }
        else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/folder_list_cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            folderNameLabel.setText(folder.getName());
            Image image = new Image(getClass().getResource("/img/" + folder.getLogo()).toExternalForm());
            folderIconImageView.setImage(image);
            setText(null);
            setGraphic(anchorPane);
        }
    }
}
