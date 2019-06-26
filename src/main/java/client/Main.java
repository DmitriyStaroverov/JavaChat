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
        FXMLLoader loader = new FXMLLoader ( getClass ().getResource ("/fxml/sample.fxml") );
        Parent root = loader.load ();
        // передача сслыки на сцену в контроллер
        Controller controller = loader.getController ();
        controller.setStage ( primaryStage );
        //
        primaryStage.setTitle("Java Chat :");
        primaryStage.getIcons ().add(new Image ( "/images/hashtag.png" )) ;
        primaryStage.setScene(new Scene(root, 640, 480));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
