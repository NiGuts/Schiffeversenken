package battleships.GUI;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Super Klasse, ist zuständig für Methoden die häufig benötigt werden.
 */
public class SuperController {
    @FXML
    FXMLLoader fxmlLoader;
    private static int tableSize;
    private static double fieldsize;
    static String[][] myBoard;
    Scene scene;

    /**
     * Setzt die Tabellengröße.
     *
     * @param newtableSize
     * @param newfieldsize
     */
    public static void setTableSize(int newtableSize, double newfieldsize) {
        tableSize = newtableSize;
        fieldsize = newfieldsize;
    }

    public static int getTableSize() {
        return tableSize;
    }

    public static double getFieldsize() {
        return fieldsize;
    }

/*
Die vorherige Methode changeScene(String s, Event event) Methode
wird hier überladen, damit man statt einem Event auch ein Node
übergeben kann.
 */

    /**
     * Zuständig für die Scenenwechsel.
     *
     * @param s
     * @param event
     */
    public void changeScene(String s, Event event) {
        changeScene(s, (Node) event.getSource());
    }

    /**
     * Zuständig für die Scenenwechsel
     *
     * @param s
     * @param node
     */
    public void changeScene(String s, Node node) {
        try {
            scene = node.getScene();
            fxmlLoader = new FXMLLoader(getClass().getResource(s + ".fxml"));
            Parent root = fxmlLoader.load();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Datei nicht Gefunden\n");
        }
    }

    /**
     * Animationen.
     *
     * @param whatkind_of_Object
     * @param fxID
     * @param event
     * @param howMuchScaleDoYouWant
     * @param duration
     */
    public void ScaleAnimation(String whatkind_of_Object, String fxID, MouseEvent event, double howMuchScaleDoYouWant, Double duration) {
        if (whatkind_of_Object != null && fxID != null && event != null) {

            scene = ((Node) event.getSource()).getScene();

            if (whatkind_of_Object.equals("I")) {        // if the object to scale is an ImageView
                ImageView object = (ImageView) scene.lookup("#" + fxID);
                KeyValue keyValue0 = new KeyValue(object.scaleXProperty(), howMuchScaleDoYouWant, Interpolator.EASE_BOTH);
                KeyValue keyValue1 = new KeyValue(object.scaleYProperty(), howMuchScaleDoYouWant, Interpolator.EASE_BOTH);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), keyValue0, keyValue1);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                return;
            }
            if (whatkind_of_Object.equals("B")) {        // if the Object to scale is a Button
                Button object = (Button) scene.lookup("#" + fxID);
                KeyValue keyValue0 = new KeyValue(object.scaleXProperty(), howMuchScaleDoYouWant, Interpolator.EASE_BOTH);
                KeyValue keyValue1 = new KeyValue(object.scaleYProperty(), howMuchScaleDoYouWant, Interpolator.EASE_BOTH);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), keyValue0, keyValue1);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                return;
            }
        }
    }

    /**
     * Animationen.
     *
     * @param whatkind_of_Object
     * @param fxID
     * @param event
     * @param duration
     */
    public void ScaleAnimation(String whatkind_of_Object, String fxID, MouseEvent event, double duration) {

        if (whatkind_of_Object != null && fxID != null && event != null) {

            scene = ((Node) event.getSource()).getScene();

            if (whatkind_of_Object.equals("I")) {        // if the object to scale is an ImageView
                ImageView object = (ImageView) scene.lookup("#" + fxID);
                KeyValue keyValue0 = new KeyValue(object.scaleXProperty(), 1.0, Interpolator.EASE_BOTH);
                KeyValue keyValue1 = new KeyValue(object.scaleYProperty(), 1.0, Interpolator.EASE_BOTH);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), keyValue0, keyValue1);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                return;
            }
            if (whatkind_of_Object.equals("B")) {        // if the Object to scale is a Button
                Button object = (Button) scene.lookup("#" + fxID);
                KeyValue keyValue0 = new KeyValue(object.scaleXProperty(), 1.0, Interpolator.EASE_BOTH);
                KeyValue keyValue1 = new KeyValue(object.scaleYProperty(), 1.0, Interpolator.EASE_BOTH);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(duration), keyValue0, keyValue1);
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().add(keyFrame);
                timeline.play();
                return;
            }
        }
    }

    /**
     * Schiffe werden erkannt die intern mit "S" gekennzeichnet wurden, dann wird
     * das passende Schiff erzeugt.
     *
     * @param shipsize
     * @return
     */
    public ImageView chooseShip(int shipsize) {
        int id = 0;
        ImageView newShip = new ImageView();
        switch (shipsize) {
            case 2:
                newShip.setImage(new Image("@../../images/2erSchiff.png"));
                newShip.setId("2erSchiff" + id);
                newShip.setFitWidth(120.0);
                newShip.setFitHeight(60.0);
                break;
            case 3:
                newShip.setImage(new Image("@../../images/3erSchiff.png"));
                newShip.setId("3erSchiff" + id);
                newShip.setFitWidth(180.0);
                newShip.setFitHeight(60.0);
                break;
            case 4:
                newShip.setImage(new Image("@../../images/4erSchiff.png"));
                newShip.setId("4erSchiff" + id);
                newShip.setFitWidth(238.8);
                newShip.setFitHeight(60.0);
                break;
            case 5:
                newShip.setImage(new Image("@../../images/5erSchiff.png"));
                newShip.setId("5erSchiff" + id);
                newShip.setFitWidth(341.6);
                newShip.setFitHeight(70.0);
                break;
        }

        return newShip;
    }

    /**
     * Schiffe werden passend zur Feldgröße scaliert.
     *
     * @param ship
     * @param fieldsize
     */
    public void setShipSize(ImageView ship, double fieldsize) {
        ship.setScaleX((fieldsize) / ship.getFitHeight());
        ship.setScaleY((fieldsize) / ship.getFitHeight());
    }
}
