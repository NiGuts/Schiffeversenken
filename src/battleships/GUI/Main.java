package battleships.GUI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    Scene menu;


    @Override
    public void start(Stage primarystage) throws Exception {
        Parent menufxml = FXMLLoader.load(getClass().getResource("Menu.fxml"));
        menu = new Scene(menufxml);

        primarystage.setTitle("Schiffe Versenken");
        primarystage.setScene(menu);
        primarystage.show();
        primarystage.setFullScreen(true);

    }


    public static void main(String[] args) {
        launch(args);
    }
}