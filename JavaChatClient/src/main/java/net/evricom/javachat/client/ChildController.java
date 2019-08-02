package net.evricom.javachat.client;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ChildController implements Initializable {

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    private Model model;

    private String receiver;

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    void setModel(Model model) {
        this.model = model;
    }

    public void sendMsg() {
        model.send("/w " + receiver + " " + textField.getText());
        textField.clear();
        textField.requestFocus();
    }

    public void toSmile() {
        textField.appendText(":)");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(location);

    }
}
