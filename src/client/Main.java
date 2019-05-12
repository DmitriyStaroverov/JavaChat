package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader ( getClass ().getResource ( "sample.fxml" ) );
        Parent root = loader.load ();
        primaryStage.setTitle("Java Chat");
        primaryStage.getIcons ().add(new Image ( "file:src/img/hashtag.png" )) ;
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
