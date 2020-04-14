package battleships.GUI;


import battleships.logic.GameOverException;
import battleships.logic.Launcher;
import battleships.network.Server;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller für das Menu.
 */
public class ControllerMenu extends SuperController implements Initializable {
    @FXML
    VBox Win_Vbox, Lose_Vbox;
    @FXML
    ImageView VerlorenBild;

    /**
     * Intialisiert wichtige elemente.
     *
     * @param url
     * @param resourceBundle
     */
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (GameOverException.foo != null) //Wenn GameOver Exception schonmal aufgerufen
        {
            if (GameOverException.winner && Win_Vbox != null && Lose_Vbox != null) {
                Win_Vbox.setVisible(true);
            } else {
                VerlorenBild.setVisible(true);
                Lose_Vbox.setVisible(true);
            }
            GameOverException.foo = null;
        }

    }

    /**
     * Wechselt auf die Scene Einzlspieler.
     *
     * @param event
     */
    //Player1 Button Action um auf Player1 modi zu wechseln
    @FXML
    public void onePlayerMode(ActionEvent event) {
        Launcher.type = "Single Player"; //lokales Spiel gegen KI
        Launcher.ai = false;
        changeScene("Player1", event);
    }

    /**
     * Wechselt zur Scene Zweispieler.
     *
     * @param event
     */
    @FXML
    public void PVP(ActionEvent event) {
        Launcher.ai = false;
        changeScene("PVP", event);
    }

    /**
     * Wechselt zur Scene KI gegen KI.
     *
     * @param event
     */
    public void KIvsKI(ActionEvent event) {
        Launcher.ai = true;  //Der Spieler ist jetzt eine KI (also KI gegen KI)
        changeScene("PVP", event);
    }

    /**
     * Darstellungen für Einstellungen.
     */
    public void QuitFromEinstellung() {

        Path path = new Path();
        path.getElements().add(new MoveTo((RecEinstellung.getWidth() / 2) + 10, (RecEinstellung.getHeight() / 2) - 1));
        path.getElements().add(new LineTo(500, (RecEinstellung.getHeight() / 2) - 1));

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(500));
        pathTransition.setNode(RecEinstellung);
        pathTransition.setPath(path);
        pathTransition.setInterpolator(Interpolator.EASE_BOTH);

        //Hbox movement ----------------------------------

        Path path2 = new Path();
        path2.getElements().add(new MoveTo((RecEinstellung.getWidth() / 2) + 10, (HboxEinstellung.getHeight() / 2) - 1));
        path2.getElements().add(new LineTo(500, (HboxEinstellung.getHeight() / 2) - 1));

        PathTransition pathTransition2 = new PathTransition();
        pathTransition2.setDuration(Duration.millis(500));
        pathTransition2.setNode(HboxEinstellung);
        pathTransition2.setPath(path2);
        pathTransition2.setInterpolator(Interpolator.EASE_BOTH);

        pathTransition.play();
        pathTransition2.play();

        //End Animation

    }

    @FXML
    Rectangle RecEinstellung;
    @FXML
    HBox HboxEinstellung;

    /**
     * Bewältigt die Animation für das Einstellungsmenu.
     *
     * @param mouseEvent Klick Event auf den Button
     */
    public void Einstellung(MouseEvent mouseEvent) {

        ((TextField) ((Node) mouseEvent.getSource()).getScene().lookup("#IPadress")).setOpacity(1.0);
        //Animation for Menu---------------------------------------
        //Rec's movement -----------------------------
        RecEinstellung.setVisible(true);
        HboxEinstellung.setVisible(true);

        Path path = new Path();
        path.getElements().add(new MoveTo(500, (RecEinstellung.getHeight() / 2) - 1));
        path.getElements().add(new LineTo((RecEinstellung.getWidth() / 2) + 10, (RecEinstellung.getHeight() / 2) - 1));

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(500));
        pathTransition.setNode(RecEinstellung);
        pathTransition.setPath(path);
        pathTransition.setInterpolator(Interpolator.EASE_BOTH);

        //Hbox movement ----------------------------------

        Path path2 = new Path();
        path2.getElements().add(new MoveTo(500, (HboxEinstellung.getHeight() / 2) - 1));
        path2.getElements().add(new LineTo((RecEinstellung.getWidth() / 2) + 10, (HboxEinstellung.getHeight() / 2) - 1));

        PathTransition pathTransition2 = new PathTransition();
        pathTransition2.setDuration(Duration.millis(500));
        pathTransition2.setNode(HboxEinstellung);
        pathTransition2.setPath(path2);
        pathTransition2.setInterpolator(Interpolator.EASE_BOTH);

        pathTransition.play();
        pathTransition2.play();

        //End Animation

        YourIPAdress(mouseEvent);

    }

    /**
     * Ist verantwortlich für die Rotieranimation des Steuerrads, in dem man über das Steurerrad hovert.
     *
     * @param event Wird übergeben um auf das Objekt zuzugreifen
     */
    public void RotateWheel(MouseEvent event) {

        ImageView image = (ImageView) ((Node) event.getSource()).getScene().lookup("#Einstellungen");
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.seconds(1),
                new KeyValue(image.rotateProperty(), 360)
        ));
        timeline.play();
    }

    /**
     * Macht das selbe wie die RotateWheel nur rückwärts.
     *
     * @param event
     */
    public void RotateDWheel(MouseEvent event) {
        Timeline timeline = new Timeline();
        ImageView image = (ImageView) ((Node) event.getSource()).getScene().lookup("#Einstellungen");
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.seconds(1),
                new KeyValue(image.rotateProperty(), 0)
        ));
        timeline.play();
    }
    // End of Rotating Stuff -----------------------------------------------------

    /**
     * Methode ist nur Desgin technisch.
     *
     * @param mouseEvent
     */
    public void QuitEinstellungMouseEntered(MouseEvent mouseEvent) {
        ((ImageView) mouseEvent.getSource()).setStyle("-fx-opacity: 1.0");
    }

    /**
     * Nur Design technisch.
     *
     * @param mouseEvent
     */
    public void QuitEinstellungMouseExit(MouseEvent mouseEvent) {
        ((ImageView) mouseEvent.getSource()).setStyle("-fx-opacity: 0.7");
    }

    /**
     * Stellt das Spiel in den Einstellungen auf Vollbildschirm.
     *
     * @param event
     */
    public void FullscreenMode(ActionEvent event) {

        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setFullScreen(true);
    }

    /**
     * Stellt das Spiel in den Einstellungen auf Fenster Modus.
     *
     * @param event
     */
    public void WindowMode(ActionEvent event) {

        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        stage.setFullScreen(false);
        stage.setHeight(800.0);
        stage.setWidth(1200.0);

    }

    /**
     * Beendet das Spiel über dem Button "Aus" oben rechts im Spiel.
     */
    public void quit() {
        System.exit(0);
    }

    /**
     * Vollführt nur eine Animation.
     *
     * @param event
     */
    public void hoverquit(MouseEvent event) {
        ScaleAnimation("I", "Shutdown", event, 1.2, 100.0);
    }

    /**
     * Design technisch.
     *
     * @param event
     */
    public void hoverquitexited(MouseEvent event) {
        ScaleAnimation("I", "Shutdown", event, 100.0);
    }
    // end of Animation
    //------------------------------- belongs all to the Shutdown Button ----- ^ ---

    /**
     * Design technisch.
     *
     * @param mouseEvent
     */
    //-------------- Handled the Animation from the Buttons in Menu-------------//
    public void hoverButtonEntered(MouseEvent mouseEvent) {

        Scene scene1 = ((Node) mouseEvent.getSource()).getScene();
        String string = ((Node) mouseEvent.getSource()).getId();
        if (scene1 != null) {
            ScaleAnimation("B", string, mouseEvent, 1.05, 100.0);
        }
    }

    /**
     * Design technisch.
     *
     * @param mouseEvent
     */
    public void hoverButtonExited(MouseEvent mouseEvent) {

        Scene scene1 = ((Node) mouseEvent.getSource()).getScene();
        String string = ((Node) mouseEvent.getSource()).getId();
        if (scene1 != null) {

            ScaleAnimation("B", string, mouseEvent, 100.0);
        }
    }
    //-------------------------------------------------------------------//

    /**
     * Gibt die IP-Adresse bei den Einstellungen an.
     *
     * @param event
     */
    public void YourIPAdress(MouseEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        TextField textField = (TextField) scene.lookup("#IPadress");

        try {

            //String s = InetAddress.getLocalHost().getHostAddress();
            String s = Server.getIP();
            textField.setText(s);

        } catch (Exception exception) {

            textField.setText("FEHLGESCHLAGEN");

            System.err.println("FEHLGESCHLAGEN");

        }
    }

    /**
     * Methode wechselt zum Menu, beim Ende eines Spiels.
     *
     * @param event
     */
    public void changeScene_fromWinLose_toMenu(ActionEvent event) {
        changeScene("Menu", event);
    }

}
