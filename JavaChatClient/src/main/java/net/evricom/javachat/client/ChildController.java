package net.evricom.javachat.client;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChildController {

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

}
