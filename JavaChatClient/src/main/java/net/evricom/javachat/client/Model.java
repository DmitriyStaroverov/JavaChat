package net.evricom.javachat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Model extends Application {

    static final String appName = "Java Chat";

    private static final String IP_ADRESS = InetAddress.getLoopbackAddress().getHostAddress();

    private static final int PORT = 8189;

    boolean isAuthorized;

    private Controller controller;

    String nickName = "";

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    ArrayList<String> blackList = new ArrayList<>();

    ArrayList<String> clientList = new ArrayList<>();

    private void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
        if (!authorized) nickName = "";
        controller.changeAuthorize();
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(() -> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        if (StringUtils.startsWith(str, "/authok")) {
                            String[] tokens = str.split(" ");
                            nickName = tokens[1];
                            setAuthorized(true);
                            break;
                        } else if (StringUtils.startsWith(str, "/TIMEOUT")) {
                            controller.showMsg(str);
                            send("/end");
                        } else if (StringUtils.startsWith(str, "/serverClosed")) {
                            break;
                        } else {
                            controller.showMsg(str);
                        }
                    }
                    while (isAuthorized) {
                        String str = in.readUTF();
                        if (StringUtils.startsWith(str, "/serverClosed")) break;
                        if (StringUtils.startsWith(str, "/clientList")) {
                            clientList.clear();
                            String[] tokens = str.split(" ");
                            clientList.addAll(Arrays.asList(tokens).subList(1, tokens.length));
                            controller.setClientList();
                            continue;
                        }
                        if (StringUtils.startsWith(str, "/history")) {
                            controller.clearArea();
                            String[] tokens = str.split(" ",2);
                            controller.showMsg(tokens[1]);
                            continue;
                        }
                        if (StringUtils.startsWith(str, "/blacklist")) {
                            blackList.clear();
                            String[] tokens = str.split(" ");
                            blackList.addAll(Arrays.asList(tokens).subList(1, tokens.length));
                            controller.setClientList();
                            continue;
                        }
                        controller.showMsg(str);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setAuthorized(false);
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void tryToAuth(String login, String pass) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF("/auth " + login + " " + pass);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addItemForBlackList(String strItem) {
        send("/blacklist ADD " + strItem);
    }

    public void deleteItemForBlackList(String strItem) {
        send("/blacklist DEL " + strItem);
    }

    void newPrivateChat(String receiver) {
        if (StringUtils.isEmpty(receiver)) return;
        if (StringUtils.equals(receiver, nickName)) return;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/ChildWindow.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Private Chat " + nickName + " to " + receiver);
            // передача сслыки на родительский контроллер
            ChildController childController = fxmlLoader.getController();
            childController.setModel(this);
            childController.setReceiver(receiver);
            stage.setScene(new Scene(root1));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ParentWindow.fxml"));
        Parent root = loader.load();
        // передача сслыки на контроллер
        controller = loader.getController();
        controller.setModel(this);
        controller.setStage(primaryStage);
        //
        primaryStage.setTitle("Java Chat :");
        primaryStage.getIcons().add(new Image("/images/hashtag.png"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        //
        // close window
        primaryStage.setOnCloseRequest(event -> {
            send("/end");
            //event.consume(); если нужно отменить закрытие
        });

    }


    public static void main(String[] args) {
        launch(args);
    }

}

/*
FIXME
 3 Добавить роль администратора (который может удалять клиентов из чата)
 5 Отображать дату и время в сообщениях и истории
 6 Сообщения должны быть разного цвета
*/
