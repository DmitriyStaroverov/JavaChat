package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import static javafx.application.Platform.exit;

public class Controller {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    private String nickName = "user1";

    public void sendMsg() {
        Date date = new Date ();
        SimpleDateFormat timeFormat = new SimpleDateFormat ("[H:mm:ss]");
        textArea.appendText(timeFormat.format(date) + " " + nickName + ": " + textField.getText() + "\n");
        textField.clear ();
        textField.requestFocus ();
    }

    public void menuProfile(ActionEvent actionEvent) {
        Dialog dialogProfile = new Dialog ();
        dialogProfile.setTitle ( "User profile" );
        dialogProfile.setHeaderText("Profile settings");
        ButtonType btnSaveProfile = new ButtonType ( "OK", ButtonBar.ButtonData.OK_DONE );
        dialogProfile.getDialogPane ().getButtonTypes ().addAll (btnSaveProfile, ButtonType.CANCEL  );
        //
        GridPane gridPane = new GridPane ();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets (20, 150, 10, 10));
        gridPane.add(new Label("Username"), 0, 0);
        //
        TextField tfNickName = new TextField ( this.nickName );
        gridPane.add(tfNickName, 1, 0);
        //
        dialogProfile.getDialogPane ().setContent ( gridPane );
        //
        Optional<ButtonType> result = dialogProfile.showAndWait();
        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            nickName = tfNickName.getText ();
        }
    }

    public void menuClear(ActionEvent actionEvent) {
        Alert alert = new Alert ( Alert.AlertType.WARNING, "", ButtonType.YES, ButtonType.NO );
        alert.setTitle ( "Очистить историю" );
        alert.setHeaderText ( null );
        alert.setContentText ( "Вы действительно хотите удалить все сообщения?" );
        Optional<ButtonType> result = alert.showAndWait ();
        if ((result.isPresent ()) && (result.get () == ButtonType.YES)) {
            this.textArea.clear ();
        }
    }

    public void menuCloseApp() {
       exit ();
    }

    public void menuHelp(ActionEvent actionEvent) {
        Alert alertHelp = new Alert(Alert.AlertType.INFORMATION);
        alertHelp.setTitle("Help");
        alertHelp.setHeaderText(null);
        alertHelp.setContentText("Ничем не могу помочь");
        alertHelp.showAndWait();
    }



    public void toSmile(ActionEvent actionEvent) {
        textField.appendText ( ":)" );
    }
}


