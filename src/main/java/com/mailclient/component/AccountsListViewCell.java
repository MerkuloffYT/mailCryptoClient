package com.mailclient.component;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AccountsListViewCell extends ListCell<String> {
    @FXML private Label accountLabel;
    @FXML private AnchorPane anchorPane;

    FXMLLoader fxmlLoader;

    @Override
    protected void updateItem(String account, boolean empty){
        super.updateItem(account,empty);
        if(empty || account == null){
            setText(null);
            setGraphic(null);
        }
        else {
            if(fxmlLoader == null){
                fxmlLoader = new FXMLLoader(getClass().getResource("/view/account_cell.fxml"));
                fxmlLoader.setController(this);
                try {
                    fxmlLoader.load();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            accountLabel.setText(account);
            setText(null);
            setGraphic(anchorPane);
        }
    }
}
