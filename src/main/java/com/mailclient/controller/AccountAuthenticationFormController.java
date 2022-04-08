package com.mailclient.controller;

import com.mailclient.entity.MailSession;
import com.mailclient.service.AccountService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("/view/account_authentication_form.fxml")
public class AccountAuthenticationFormController implements Initializable {

    private final FxWeaver fxWeaver;
    private final ApplicationContext context;

    @FXML public ImageView closeButton;
    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button addAccountButton;

    @Autowired
    public AccountAuthenticationFormController(FxWeaver fxWeaver, ApplicationContext context){
        this.fxWeaver = fxWeaver;
        this.context = context;
    }

    @FXML
    public void handleKeyTypedTextField(KeyEvent e){
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        if(!email.trim().isEmpty() && !password.equals("")) {
            if (email.matches("^.*@(mail[.]ru|gmail[.]com|yandex[.]ru)$")) {
                loginButton.setDisable(false);
                errorLabel.setText("");
            } else {
                errorLabel.setText("Проверьте корректность введённых данных");
            }
        }
    }

    @FXML
    public void handleLoginButtonClicked(MouseEvent e){
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        try {
            AccountService.setCurrentMailSession(context.getBean(MailSession.class, email, password));
            List<String> accounts = AccountService.getAccounts();
            boolean exists = false;
            for(String account : accounts) {
                if (account.equals(email)) {
                    exists = true;
                }
            }
            if(!exists) {
                AccountService.putMailSession(email, AccountService.getCurrentMailSession());
            }
            System.out.println("Authentication attempt for " + email);

            Stage stage = ((Stage) loginButton.getScene().getWindow());
            Scene scene = new Scene(fxWeaver.loadView(MainFormController.class));
            if (MainFormController.mainStage != null) {
                stage.setX(MainFormController.mainStage.getX());
                stage.setY(MainFormController.mainStage.getY());
                MainFormController.mainStage.close();
            }
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLabel.setText("Неверный логин или пароль");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка аутентификации");
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) { }
}
