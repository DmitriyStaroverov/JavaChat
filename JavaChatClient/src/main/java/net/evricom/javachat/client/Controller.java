package net.evricom.javachat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static javafx.application.Platform.exit;

public class Controller implements Initializable {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    HBox upperPanel;

    @FXML
    HBox bottomPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;

    @FXML
    ListView<String> clientList;

    ObservableList<String> observableListClients;

    private Model model;

    void setModel(Model model) {
        this.model = model;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private Stage stage;

    private void setCaptionApp() {
        stage.setTitle(Model.appName + " " + model.nickName);
    }

    void changeAuthorize() {
        Platform.runLater(() -> {
            setCaptionApp();
            if (!model.isAuthorized) {
                upperPanel.setVisible(true);
                upperPanel.setManaged(true);
                bottomPanel.setVisible(false);
                bottomPanel.setManaged(false);
                clientList.setManaged(false);
                clientList.setVisible(false);
            } else {
                upperPanel.setVisible(false);
                upperPanel.setManaged(false);
                bottomPanel.setVisible(true);
                bottomPanel.setManaged(true);
                clientList.setManaged(true);
                clientList.setVisible(true);
            }
        });
    }

    public void sendMsg() {
        model.send(textField.getText());
        textField.clear();
        textField.requestFocus();
    }

    void showMsg(String msg) {
        textArea.appendText(msg + "\n");
    }

    public void menuProfile() {
        Dialog dialogProfile = new Dialog();
        dialogProfile.setTitle("User profile");
        dialogProfile.setHeaderText("Profile settings");
        ButtonType btnSaveProfile = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialogProfile.getDialogPane().getButtonTypes().addAll(btnSaveProfile, ButtonType.CANCEL);
        //
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20, 150, 10, 10));
        gridPane.add(new Label("Username"), 0, 0);
        //
        TextField tfNickName = new TextField(model.nickName);
        tfNickName.setEditable(false);

        gridPane.add(tfNickName, 1, 0);
        //
        dialogProfile.getDialogPane().setContent(gridPane);
        //
        //Optional<ButtonType> result = dialogProfile.showAndWait ();
        //if (result.isPresent () && result.get ().getButtonData () == ButtonBar.ButtonData.OK_DONE) {
        //nickName = tfNickName.getText ();
        //}
    }

    public void menuClear() {
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Очистить историю");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить все сообщения?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            this.textArea.clear();
        }
    }


    public void menuCloseApp() {
        exit();
    }

    public void menuHelp() {
        Alert alertHelp = new Alert(Alert.AlertType.INFORMATION);
        alertHelp.setTitle("Help");
        alertHelp.setHeaderText(null);
        alertHelp.setContentText("????????");
        alertHelp.showAndWait();
    }


    public void toSmile() {
        textField.appendText(":)");
    }

    public void authBtn() {
        model.tryToAuth(loginField.getText(), passwordField.getText());
        loginField.clear();
        passwordField.clear();
    }

    void setClientList() {
        Platform.runLater(() -> {
            observableListClients.clear();
            for (String strClient:
                 model.clientList) {
                observableListClients.add(strClient);
            }
        });
    }

    private Label createLebelItem(String strItem) {
        Label lb1 = new Label(strItem);
        if (StringUtils.equals(strItem, model.nickName)) {
           lb1.setId("labelThisClient");
        } else if (model.blackList.contains(strItem)) {
            lb1.setId("labelBlackListClient");
            createContextMenuBlackList(lb1);
        } else {
            lb1.setId("labelNormClient");
            createContextMenuNorm(lb1);
        }
        return lb1;
    }

    private void createContextMenuNorm(Label lb1) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem1 = new MenuItem("Приватный чат");
        menuItem1.setOnAction(event -> model.newPrivateChat(lb1.getText()));
        MenuItem menuItem2 = new MenuItem("Внести в черный список");
        menuItem2.setOnAction(event -> model.addItemForBlackList(lb1.getText()));
        contextMenu.getItems().addAll(menuItem1, menuItem2);
        lb1.setOnContextMenuRequested(event -> contextMenu.show(lb1,event.getScreenX(), event.getScreenY()));
    }

    private void createContextMenuBlackList(Label lb1) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Убрать из черного списка");
        menuItem.setOnAction(event -> model.deleteItemForBlackList(lb1.getText()));
        contextMenu.getItems().add(menuItem);
        lb1.setOnContextMenuRequested(event -> contextMenu.show(lb1,event.getScreenX(), event.getScreenY()));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        observableListClients = FXCollections.observableArrayList();
        clientList.setItems(observableListClients);
        clientList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String strItem, boolean empty) {
                        super.updateItem(strItem, empty);
                        if (empty || strItem == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setGraphic(createLebelItem(strItem));
                        }
                    }
                };
            }
        });
    }
}


