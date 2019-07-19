package net.evricom.javachat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Optional;

import static javafx.application.Platform.exit;

public class Controller {

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

    private Model model;

    void setModel(Model model) {
        this.model = model;
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    private Stage stage;

    private void setCaptionApp(){
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

    public void mouseClicked() {

        model.newPrivateChat(clientList.getSelectionModel().getSelectedItem());


    }

    void setItemsClientList(String str) {
        String[] tokens = str.split(" ");
        Platform.runLater(() -> {
            clientList.getItems().clear();
            for (int i = 1; i < tokens.length; i++) {
                clientList.getItems().add(tokens[i]);
            }
        });
    }
}


