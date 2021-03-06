package com.mailclient.controller;

import com.mailclient.component.AccountsListViewCell;
import com.mailclient.component.FolderListViewCell;
import com.mailclient.entity.AccountFolder;
import com.mailclient.entity.MailSession;
import com.mailclient.service.AccountService;
import com.mailclient.service.MailService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
@Component
@FxmlView("/view/main_form.fxml")
public class MainFormController implements Initializable {

    @FXML private Button deleteAccountButton;
    @FXML private Button addAccountButton;
    @FXML private ListView<AccountFolder> foldersListView;
    @FXML private ListView<String> accountsListView;
    @FXML private AnchorPane mainPane;

    ApplicationContext context;
    MailService mailService;

    public static Stage mainStage;

    @Autowired
    MainFormController(ApplicationContext context, MailService mailService) {
        this.context = context;
        this.mailService = mailService;
    }

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        foldersListView.setItems(getAccountFolders());
        foldersListView.setCellFactory(folderListView -> new FolderListViewCell());
        accountsListView.setItems(FXCollections.observableArrayList(AccountService.getAccounts()));
        accountsListView.setCellFactory(accountsListView -> new AccountsListViewCell());

        MailSession mailSession = AccountService.getCurrentMailSession();
        selectCellInListViews(mailSession);

        MailListFormController mailListFormController = new MailListFormController(
                mailSession.getFolder("inbox"), 1, mailService, mailSession, mainPane
        );
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_list_form.fxml"));
        loader.setController(mailListFormController);
        mainPane.getChildren().add(loader.load());
    }

    /**
     * ?????????????????? ???????????? ?????????? ?? ???????????????? ???? ???????????????? ?????????? ?? ??????????????????
     * @param mailSession ?????????????? ????????????
     */
    private void selectCellInListViews(MailSession mailSession) {
        for(String email : AccountService.getAccounts()) {
            if (mailSession.getEmail().equals(email)) {
                accountsListView.getSelectionModel().select(email);
            }
        }
        foldersListView.getSelectionModel().select(1);
    }

    /**
     * @return ???????????????????? ?????????????? ?????????? (1-???? ?????????? - ???? ??????????, ???????????? ???????? ????????????)
     */
    private ObservableList<AccountFolder> getAccountFolders() {
        AccountFolder[] folders = new AccountFolder[] {
                new AccountFolder("?????????? ????????????", "new-mail.png"),
                new AccountFolder("????????????????", "inbox-folder.png"),
                new AccountFolder("????????????????????????", "outbox-folder.png"),
                new AccountFolder("??????????????????", "draft-folder.png"),
                new AccountFolder("??????????????", "trash-folder.png")
        };
        return FXCollections.observableArrayList(folders);
    }

    @SneakyThrows
    @FXML
    public void handleFoldersListViewClick(MouseEvent event) {
        String folderName = getSelectedFolderName();
        MailSession mailSession = AccountService.getCurrentMailSession();
        if (folderName != null && folderName.equals("new mail")) {
            System.out.println(mailSession.getEmail() + ": creating " + folderName);
            handleOnCreateNewMailButton();
            return;
        }
        MailListFormController mailListFormController = new MailListFormController(
                mailSession.getFolder(folderName), 1, mailService, mailSession, mainPane
        );
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_list_form.fxml"));
        loader.setController(mailListFormController);
        mainPane.getChildren().clear();
        mainPane.getChildren().add(loader.load());
        System.out.println(mailSession.getEmail() + ": Selected folder " + folderName);
    }

    /**
     * @return ???????????????????????? ?????????? ?? ???????????????? ?????????? ???????????? ???? ????????????????
     */
    private String getSelectedFolderName() {
        switch (foldersListView.getSelectionModel().getSelectedItem().getName()) {
            case "????????????????": return "inbox";
            case "????????????????????????": return "sent";
            case "??????????????????": return "drafts";
            case "??????????????": return "trash";
            case "?????????? ????????????": return "new mail";
            default: return null;
        }
    }

    @SneakyThrows
    @FXML
    public void handleOnAccountsListViewClick(MouseEvent event) {
        String selectedAccount = accountsListView.getSelectionModel().getSelectedItem();
        String currentAccount = AccountService.getCurrentMailSession().getEmail();
        System.out.println(AccountService.getCurrentMailSession().getEmail() + ": Selected account " + selectedAccount);

        if(!selectedAccount.equals(currentAccount)) {
            MailSession currentMailSession = AccountService.getMailSession(selectedAccount);
            AccountService.setCurrentMailSession(currentMailSession);
            String selectedFolderName = getSelectedFolderName();
            MailListFormController mailListFormController = new MailListFormController(
                    currentMailSession.getFolder(selectedFolderName != null ? selectedFolderName : "inbox"),
                    mailService,
                    currentMailSession,
                    mainPane
            );

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_list_form.fxml"));
            loader.setController(mailListFormController);
            mainPane.getChildren().remove(0, mainPane.getChildren().size());
            mainPane.getChildren().add(loader.load());
            System.out.println(AccountService.getCurrentMailSession().getEmail() + ": account changed to" + selectedAccount);
        }
    }

    @SneakyThrows
    @FXML
    public void handleOnCreateNewMailButton() {
        MailWritingFormController mailWritingFormController = new MailWritingFormController(mainPane, mailService);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_writing_form.fxml"));
        loader.setController(mailWritingFormController);
        if(mainPane.getChildren().size() > 0) {
            mainPane.getChildren().get(0).setVisible(false);
        }
        mainPane.getChildren().add(loader.load());
    }

    @FXML
    public void handleOnAddAccountButtonClick(MouseEvent e) {
        mainStage = ((Stage) mainPane.getScene().getWindow());
        showAuthentificationForm();
    }

    /**
     * ???????????????????? ?????????? ????????????????????????????
     */
    private void showAuthentificationForm() {
        FxWeaver fxWeaver = context.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(AccountAuthenticationFormController.class);
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    @FXML
    public void handleOnDeleteAccountButtonClick(MouseEvent e) {
        String account = accountsListView.getSelectionModel().getSelectedItem();
        System.out.println(AccountService.getCurrentMailSession().getEmail() + ": account selected for deleting");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("???????????????? ????????????????");
        alert.setHeaderText("???? ?????????????????????????? ???????????? ?????????????? ?????????????? " + account + " ?");

        Optional<ButtonType> option = alert.showAndWait();
        if (option.isPresent() && option.get() == ButtonType.OK) {
            AccountService.deleteAccount(account);
            accountsListView.setItems(FXCollections.observableArrayList(AccountService.getAccounts()));
            accountsListView.refresh();

            Map<String, MailSession> mailSessions = AccountService.getMailSessions();
            if(mailSessions.size() == 0) {
                System.out.println("null: account " + account + " was deleted");
                showAuthentificationForm();
            } else {
                Entry<String, MailSession> entry = mailSessions.entrySet().iterator().next();
                MailSession currentMailSession = entry.getValue();

                AccountService.setCurrentMailSession(currentMailSession);
                String selectedFolder = getSelectedFolderName();
                System.out.println(currentMailSession.getEmail() + ": account was auto selected, folder " + selectedFolder + " was auto selected");

                MailListFormController mailListFormController = new MailListFormController(
                        currentMailSession.getFolder(selectedFolder), mailService, currentMailSession, mainPane
                );
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/mail_list_form.fxml"));
                loader.setController(mailListFormController);
                mainPane.getChildren().removeAll();
                mainPane.getChildren().add(loader.load());
            }
        }
    }
}
