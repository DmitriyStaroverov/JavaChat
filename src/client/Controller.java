package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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

    private boolean isAuthorized;

    private String nickName;

    final String IP_ADRESS = "127.0.0.1";

    final int PORT = 8189;

    private Stage stage;

    Socket socket;

    DataInputStream in;

    DataOutputStream out;

    void setStage (Stage stage){
        this.stage = stage;
    }

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized = isAuthorized;
        Platform.runLater(() -> {
            if (!isAuthorized){
                upperPanel.setVisible ( true );
                upperPanel.setManaged ( true );
                bottomPanel.setVisible ( false );
                bottomPanel.setManaged ( false );
                stage.setTitle ( "Java Chat :" );
            } else {
                upperPanel.setVisible ( false );
                upperPanel.setManaged ( false );
                bottomPanel.setVisible ( true );
                bottomPanel.setManaged ( true );
                stage.setTitle ( "Java Chat : " + nickName );
            }
        });
    }

    public void sendMsg() {
        try {
            out.writeUTF ( textField.getText () );
            textField.clear ();
            textField.requestFocus ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void menuProfile(ActionEvent actionEvent) {
        Dialog dialogProfile = new Dialog ();
        dialogProfile.setTitle ( "User profile" );
        dialogProfile.setHeaderText ( "Profile settings" );
        ButtonType btnSaveProfile = new ButtonType ( "OK", ButtonBar.ButtonData.OK_DONE );
        dialogProfile.getDialogPane ().getButtonTypes ().addAll ( btnSaveProfile, ButtonType.CANCEL );
        //
        GridPane gridPane = new GridPane ();
        gridPane.setHgap ( 10 );
        gridPane.setVgap ( 10 );
        gridPane.setPadding ( new Insets ( 20, 150, 10, 10 ) );
        gridPane.add ( new Label ( "Username" ), 0, 0 );
        //
        TextField tfNickName = new TextField ( this.nickName );
        tfNickName.setText ( nickName );
        tfNickName.setEditable ( false );

        gridPane.add ( tfNickName, 1, 0 );
        //
        dialogProfile.getDialogPane ().setContent ( gridPane );
        //
        Optional<ButtonType> result = dialogProfile.showAndWait ();
        if (result.isPresent () && result.get ().getButtonData () == ButtonBar.ButtonData.OK_DONE) {
            //nickName = tfNickName.getText ();
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



    public void connect() {
        try {
            socket = new Socket ( IP_ADRESS, PORT );
            in = new DataInputStream ( socket.getInputStream () );
            out = new DataOutputStream ( socket.getOutputStream () );
            setAuthorized ( false );
            Thread thread = new Thread ( new Runnable () {
                @Override
                public void run() {
                    try {
                        while (true){
                            String str = in.readUTF ();
                            if (str.indexOf ( "/authok" )> -1){
                                String[] tokens = str.split ( " " );
                                nickName = tokens[1];
                                setAuthorized ( true );
                                break;
                            } else {
                                textArea.appendText ( str + "\n");
                            }

                        }
                        while (true) {
                            String str = in.readUTF ();
                            if (str.indexOf ( "/serverClosed" ) > -1) break;
                            textArea.appendText ( str + "\n" );
                        }
                    } catch (IOException e) {
                        e.printStackTrace ();
                    } finally {
                        try {
                            socket.close ();
                        } catch (IOException e) {
                            e.printStackTrace ();
                        }
                        setAuthorized ( false );
                    }
                }
            } );
            thread.setDaemon ( true );
            thread.start ();

        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    public void menuCloseApp() {
        exit ();
    }

    public void menuHelp(ActionEvent actionEvent) {
        Alert alertHelp = new Alert ( Alert.AlertType.INFORMATION );
        alertHelp.setTitle ( "Help" );
        alertHelp.setHeaderText ( null );
        alertHelp.setContentText ( "????????" );
        alertHelp.showAndWait ();
    }


    public void toSmile(ActionEvent actionEvent) {
        textField.appendText ( ":)" );
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed ()){
            connect ();
        }
        try {
            out.writeUTF ( "/auth " + loginField.getText () + " " + passwordField.getText () );
            loginField.clear ();
            passwordField.clear ();
        } catch (IOException e) {
            e.printStackTrace ();
        }

    }
}


