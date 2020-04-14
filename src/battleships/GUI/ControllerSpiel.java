//----- Class ControllerSpiel---------//

package battleships.GUI;

import battleships.logic.*;
import javafx.animation.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


/**
 * Klasse für die Controlle des Spielablaufs.
 */
public class ControllerSpiel extends SuperController implements Initializable {

    private int size1 = getTableSize(); //size1 ist die Groesse zb. 10x10 Feld und 10 ist die size1
    private double fieldsize = getFieldsize();//ist die groesse der Rechtecke
    @FXML
    Pane hboxgridpane1;
    @FXML
    GridPane gridpane1;
    @FXML
    Pane meinSpielfeld;
    @FXML
    AnchorPane anchor;
    @FXML
    Pane paneMeinSpielfeld;
    @FXML
    Button ShotButton;
    @FXML
    Button saveButton;
    @FXML
    HBox Opponent_Hbox;
    @FXML
    Label Label_Opnnent_Shot;
    @FXML
    Pane SurePane;
    Scene scene;
    private boolean timelineAnimationRunning = false;
    private boolean pathTransitionAnimationRunning = false;
    private boolean pathTransitionAnimationRunning1 = false;
    private boolean pathTransitionAnimationRunning2 = false;

    /**
     * Initialisert wichtige Elemete in der Scene.
     *
     * @param url
     * @param resourceBundle
     */
    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        settable_spiel(null);
        if (Launcher.type.equals("Client") && Launcher.ai) { //Wenn KI als Client
            new WaitForShotResultService("-2", "-2").start(); //Übergebene Koordinaten sind belanglos, da die KI sich eigene Felder berechnet
        } else if (Launcher.type.equals("Server")) {
            switch_turn(null); //Als Server muss am Anfang auf den Schuss des Gegner gewartet werden
            new ServerBeginningWait().start();
        }
    }

    /**
     * Speichert ein Spiel wenn auf "ja" geklickt wird.
     *
     * @param event
     * @throws IOException
     */
    public void JAButton(ActionEvent event) throws IOException {
        CSVmanager.save(Launcher.player.myBoard, Launcher.player.opBoard, true, Launcher.player.myLifes, Launcher.player.lifes);
        changeScene("Menu", event);
        if (Launcher.opponent instanceof Op_Player) { //Wenn imm Mehrspielermodus oder KI gegen KI
            ((Op_Player) Launcher.opponent).closeSocket();
        }
    }

    /**
     * GUI Darstellung.
     */
    public void NeinButton() {
        SurePane.setVisible(false);
    }

    /**
     * GUI Darstellung.
     */
    public void Save_n_Quit() {
        SurePane.setVisible(true);
    }

    /**
     * Plaziert die Tabelle für's Spiel.
     *
     * @param event Button klick.
     */
    public void settable_spiel(Event event) {
        arrayToGUI(Launcher.player.myBoard, Launcher.player.opBoard);

        for (int i = 0; i < size1; i++) {
            gridpane1.getColumnConstraints().add(new ColumnConstraints(fieldsize));
            gridpane1.getRowConstraints().add(new RowConstraints(fieldsize));
        }

        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size1; j++) {
                Rectangle rectangle = new Rectangle();
                rectangle.setId(i + "/" + j);
                rectangle.setHeight(fieldsize - 2);
                rectangle.setWidth(fieldsize - 2);
                rectangle.setFill(Color.rgb(42, 92, 115)); //maybe need to change the color in the Player1 class
                rectangle.setStroke(Color.hsb(0, 0, 0.3));
                rectangle.setStyle("-fx-arc-height: 15px; -fx-arc-width: 15px");
                gridpane1.add(rectangle, i, j);
                rectangle.addEventHandler(MouseEvent.MOUSE_ENTERED, hover);
                rectangle.addEventHandler(MouseEvent.MOUSE_EXITED, hover);
                rectangle.addEventHandler(MouseEvent.MOUSE_CLICKED, clicked);

                rectangle.setStrokeWidth(0);
            }
        }
        meinSpielfeld(event, 400.0 / size1);
        gridpane1.setStyle("-fx-background-radius: 2px");
    }

    /**
     * Platziert das Spielfeld des Spieler's.
     *
     * @param event
     * @param zahl  Größe der Tabelle.
     */
    public void meinSpielfeld(Event event, double zahl) {

        meinSpielfeld.setBackground(new Background(new BackgroundFill(Color.STEELBLUE, new CornerRadii(2), Insets.EMPTY)));

        for (int i = 0; i < size1; i++) {
            for (int j = 0; j < size1; j++) {
                Rectangle rectangle = new Rectangle();
                rectangle.setId("Rect" + j + i);
                rectangle.setHeight(zahl - 2);
                rectangle.setWidth(zahl - 2);

                rectangle.setFill(Color.rgb(9, 48, 87));
                rectangle.setStroke(Color.rgb(0, 0, 0, 0.3));
                rectangle.setStyle("-fx-arc-height: 8px; -fx-arc-width: 8px;");
                rectangle.setX((i * zahl) + 1);
                rectangle.setY((j * zahl) + 1);
                meinSpielfeld.getChildren().add(rectangle);
                rectangle.setStrokeWidth(0);
            }
        }
    }

    /**
     * GUI Darstellung.
     */
    public EventHandler hover = (EventHandler<MouseEvent>) event -> {
        Rectangle rectangle = (Rectangle) event.getSource();

        if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            rectangle.setStroke(Color.LIGHTSEAGREEN);
            rectangle.setStrokeWidth(2);
        } else {
            rectangle.setStroke(Color.hsb(0, 0, 0.3));
            rectangle.setStrokeWidth(0);
        }

    };

    private boolean flag1 = true;
    private boolean Panebig = true;

    /**
     * Hilfsmethode.
     */
    public void eigenesSpielfeld() {

        meinSpielfeldAnimation(meinSpielfeld);
        meinSpielfeldAnimation(paneMeinSpielfeld);
        Panebig = !Panebig;
        flag1 = !flag1;

    }

    /**
     * HUD animation.
     *
     * @param meinSpielfeld
     */
    public void meinSpielfeldAnimation(Node meinSpielfeld) {
        timelineAnimationRunning = true;
        pathTransitionAnimationRunning = true;

        Path path = new Path();
        PathTransition pathTransition = new PathTransition();

        if (Panebig) {
            KeyValue keyValue0 = new KeyValue(meinSpielfeld.scaleXProperty(), 1.5, Interpolator.EASE_BOTH);
            KeyValue keyValue1 = new KeyValue(meinSpielfeld.scaleYProperty(), 1.5, Interpolator.EASE_BOTH);
            KeyValue opecity = new KeyValue(meinSpielfeld.opacityProperty(), 0.95, Interpolator.EASE_BOTH);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(300.0), keyValue0, keyValue1, opecity);

            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(keyFrame);
            path.getElements().add(new MoveTo(200, 200)); //  Point(Start)
            path.getElements().add(new LineTo(100, 100));     //Point (destination)

            pathTransition.setDuration(Duration.millis(300));
            pathTransition.setNode(meinSpielfeld);
            pathTransition.setPath(path);
            pathTransition.setInterpolator(Interpolator.EASE_BOTH);

            timeline.play();
            timeline.setOnFinished((e -> timelineAnimationRunning = false));
            DisHUD.setVisible(false);

        } else {

            KeyValue keyValue = new KeyValue(meinSpielfeld.scaleXProperty(), 1, Interpolator.EASE_BOTH);
            KeyValue keyValue1 = new KeyValue(meinSpielfeld.scaleYProperty(), 1, Interpolator.EASE_BOTH);
            KeyValue opecity = new KeyValue(meinSpielfeld.opacityProperty(), 1, Interpolator.EASE_BOTH);


            KeyFrame keyFrame = new KeyFrame(Duration.millis(300.0), keyValue, keyValue1, opecity);
            Timeline timeline = new Timeline();
            timeline.getKeyFrames().add(keyFrame);

            //Path path = new Path();
            path.getElements().add(new MoveTo(100, 100));
            path.getElements().add(new LineTo(200, 200));

            pathTransition.setDuration(Duration.millis(300));
            pathTransition.setNode(meinSpielfeld);
            pathTransition.setPath(path);
            pathTransition.setInterpolator(Interpolator.EASE_BOTH);
            timeline.setOnFinished((e -> timelineAnimationRunning = false));
            timeline.play();

            DisHUD.setVisible(true);
        }
        pathTransition.setOnFinished(e -> pathTransitionAnimationRunning = false);
        pathTransition.play();

    }

    /**
     * Der "Schuss" Button wird enabled.
     */
    private void shotButton_enabled() {
        ShotButton.setDisable(false);
    }

    /**
     * Der "Schuss" Button wird disabled.
     */
    private void shotButton_disabled() {
        ShotButton.setDisable(true);
    }

    static Rectangle prev_rec_pressed;
    static boolean rec_pressed = false;
    static String rec_id;
    /**
     * Makiert in der GUI das Feld auf dass geklickt wurde.
     */
    public EventHandler clicked = (EventHandler<MouseEvent>) event -> {
        Rectangle rectangle = (Rectangle) event.getSource();

        if (rec_pressed) {

            prev_rec_pressed.setFill(Color.rgb(42, 92, 115)); // opacity 1.0
            rectangle.setFill(Color.rgb(34, 139, 34)); // opacity 0.5
            rec_pressed = false;

        } else {
            rectangle.setFill(Color.rgb(34, 139, 34)); //opacity 0.5
            rec_pressed = true;

            if (prev_rec_pressed != null) {

                prev_rec_pressed.setFill(Color.rgb(42, 92, 115)); //opacity 1.0
            }
        }

        prev_rec_pressed = rectangle;
        rec_id = rectangle.getId();

        if (rectangle.getFill().equals(Color.rgb(34, 139, 34))) // compare if button is set/pressed or color is diffrent
            shotButton_enabled();
        else
            shotButton_disabled();
    };

    /**
     * Methode wird beim Laden eines Spiel benötigt.
     *
     * @param sx
     * @param sy
     * @param whatshot
     */
    public void opshot(String sx, String sy, String whatshot) {//method for load a the shots
        ImageView image = new ImageView();
        if (whatshot.equals("X"))
            image.setImage(new Image("@../../images/getroffen.png"));
        if (whatshot.equals("w"))
            image.setImage(new Image("@../../images/nichtgetroffen.png"));

        image.setFitHeight(100);
        image.setFitWidth(100);

        setShipSize(image, (paneMeinSpielfeld.getPrefWidth() / size1) - 1);  //Davor 400

        paneMeinSpielfeld.getChildren().add(image);

        double panelayoutX = paneMeinSpielfeld.getLayoutX();
        double panelayoutY = paneMeinSpielfeld.getLayoutY();
        int x = Integer.valueOf(sx);
        int y = Integer.valueOf(sy);
        double targetXScene;
        double targetYScene;
        if (!flag1) {//if the pane is big
            panelayoutX -= paneMeinSpielfeld.getPrefWidth() / 2;
            panelayoutY -= paneMeinSpielfeld.getPrefHeight() / 2;
            targetXScene = x * ((paneMeinSpielfeld.getPrefWidth() * paneMeinSpielfeld.getScaleX()) / size1) + panelayoutX;
            targetYScene = y * ((paneMeinSpielfeld.getPrefHeight() * paneMeinSpielfeld.getScaleY()) / size1) + panelayoutY;
        } else {
            targetXScene = x * (paneMeinSpielfeld.getPrefWidth() / size1) + panelayoutX;
            targetYScene = y * (paneMeinSpielfeld.getPrefHeight() / size1) + panelayoutY;
        }

        Point2D point = image.sceneToLocal(targetXScene, targetYScene);
        image.getTransforms().add(new Translate(point.getX(), point.getY()));
    }

    /**
     * Wenn geschossen wird.
     *
     * @param event
     */
    public void myshot(Event event) {//method myshot if sameone shoot from the GUI
        if (!flag1) {//Wenn HUD grad groß ist: Klein machen
            eigenesSpielfeld();
        }
        if (HUD_disabled) DisHUD();
        prev_rec_pressed.setFill(Color.rgb(42, 92, 115)); // remove color or opacity
        String[] array = rec_id.split("/");
        int x = Integer.valueOf(array[0]);
        int y = Integer.valueOf(array[1]);
        if (!(Launcher.type.equals("Single Player"))) //Animation nicht im Einzelspielermodusspieler
            switch_turn(event);
        new WaitForShotResultService(array[1], array[0]).start();
        shotButton_disabled();
    }

    /**
     * Zum Laden eines gespeichertes Spielstands.
     *
     * @param sx
     * @param sy
     * @param whatshot
     */
    public void myshot(String sx, String sy, String whatshot) {//method for load a the shots
        if (HUD_disabled && !(Launcher.type.equals("Single Player"))) {
            DisHUD();
        }

        ImageView image = new ImageView();
        if (whatshot.equals("X"))
            image.setImage(new Image("@../../images/getroffen.png"));
        if (whatshot.equals("w"))
            image.setImage(new Image("@../../images/nichtgetroffen.png"));

        image.setFitHeight(100);
        image.setFitWidth(100);
        setShipSize(image, (fieldsize - 1));
        hboxgridpane1.getChildren().add(image);

        double hboxlayoutX = hboxgridpane1.getLayoutX();
        double hboxlayoutY = hboxgridpane1.getLayoutY();
        int x = Integer.valueOf(sx);
        int y = Integer.valueOf(sy);
        double shipTargetXScene = x * fieldsize + hboxlayoutX;
        double shipTargetYScene = y * fieldsize + hboxlayoutY;
        Point2D point = image.sceneToLocal(shipTargetXScene, shipTargetYScene);
        image.getTransforms().add(new Translate(point.getX(), point.getY()));
        shotButton_disabled();
    }

    boolean roated;

    /**
     * Zeichnet die Spielfelder.
     *
     * @param myBoard Spielfeld des Spieler's
     * @param opBoard Spielfeld der Gegener's
     */
    public void arrayToGUI(String[][] myBoard, String[][] opBoard) {
        paneMeinSpielfeld.getChildren().clear(); //Löschen der alten Nodes, damit nicht doppelt gezeichnet wird
        hboxgridpane1.getChildren().clear(); ///Löschen der alten Nodes, damit nicht doppelt gezeichnet wird
        hboxgridpane1.getChildren().add(gridpane1);
        int esCounter = 0;
        ArrayList<String> shotSpeicher = new ArrayList<>();  //Speicher die Position der Treffer und zeichnet diese am Ende der funktion, damit die Schüsse grafisch über den Schiffen liegen

        //Horizentale Suche in MYBOARD nach Schiffen im Array
        for (int i = 0; i < myBoard.length; i++) {
            for (int j = 0; j < myBoard.length; j++) {
                if (myBoard[i][j] != null && (myBoard[i][j].equals("S") || myBoard[i][j].equals("X"))) {
                    esCounter++;
                    //Wenn das nächste Zeichen kein S ist und das Schiff min 2 länge hat
                    if (j < myBoard.length - 1 && ((myBoard[i][j + 1] != null && !(myBoard[i][j + 1].equals("S") || myBoard[i][j + 1].equals("X"))) || myBoard[i][j + 1] == null) && esCounter >= 2) {
                        mycreateNewShip(esCounter, (j - (esCounter - 1)), i);
                        esCounter = 0;
                    }
                    //Wenn das Schiff bis zu rand geht
                    if (j <= myBoard.length - 1 && (myBoard[i][j].equals("S") || myBoard[i][j].equals("X")) && esCounter >= 2 && j == myBoard.length - 1) {
                        mycreateNewShip(esCounter, (j - (esCounter - 1)), i);
                        esCounter = 0;
                    }
                }
                //if a shot was there
                if (myBoard[i][j] != null && myBoard[i][j].equals("X")) {
                    shotSpeicher.add(i + "," + j);  //Speichere die Informationen der Schuesse in eine ArrayList um sie am Ende der beiden For-Schleifen zu zeichnen
                }
                //if a miss was there
                if (myBoard[i][j] != null && myBoard[i][j].equals("w")) {
                    opshot(String.valueOf(j), String.valueOf(i), myBoard[i][j]);
                }

                //wenn es nur ein S am Rand ist (vertikales schiff ist)
                if (esCounter <= 1 && j + 2 > myBoard.length)
                    esCounter = 0;

                // wenn es nur ein S ist (vertikales Schiff ist)
                if (esCounter <= 1 && j + 2 < myBoard.length && (myBoard[i][j + 1] == null || !(myBoard[i][j + 1].equals("S") || myBoard[i][j + 1].equals("X"))))
                    esCounter = 0;
            }
        }

        // Vertikale Suche in MYBOARD im Array nach Schiffen
        for (int i = 0; i < myBoard.length; i++) {
            for (int j = 0; j < myBoard.length; j++) {
                if (myBoard[j][i] != null && (myBoard[j][i].equals("S") || myBoard[j][i].equals("X"))) {
                    esCounter++;
                    //Wenn das nächste Zeichen kein S ist und das Schiff min 2 Länge hat
                    if (esCounter >= 2 && j < myBoard.length - 1 && ((myBoard[j + 1][i] != null && !(myBoard[j + 1][i].equals("S") || myBoard[j + 1][i].equals("X"))) || myBoard[j + 1][i] == null)) {
                        roated = true;
                        mycreateNewShip(esCounter, (i + 1), (j - (esCounter - 1)));
                        roated = false;
                        esCounter = 0;
                    }
                    //Wenn das Schiff bis zu rand geht
                    if (myBoard[j][i] != null && j <= myBoard.length - 1 && (myBoard[j][i].equals("S") || myBoard[j][i].equals("X")) && esCounter >= 2 && j == myBoard.length - 1) {
                        roated = true;
                        mycreateNewShip(esCounter, (i + 1), (j - (esCounter - 1)));
                        roated = false;
                        esCounter = 0;
                    }
                }

                //wenn es nur ein S am Rand ist (horizontales schiff ist)
                if (esCounter <= 1 && j + 2 > myBoard.length)
                    esCounter = 0;

                // wenn es nur ein S ist (horizontales Schiff ist)
                if (esCounter <= 1 && j + 2 < myBoard.length && (myBoard[j + 1][i] == null || !(myBoard[j + 1][i].equals("S") || myBoard[j + 1][i].equals("X"))))
                    esCounter = 0;
            }
        }
        //Zeichne die Schuesse ins Feld ein
        for (String s : shotSpeicher) {
            String[] sgeteilt = s.split(",");
            opshot(sgeteilt[1], sgeteilt[0], myBoard[Integer.parseInt(sgeteilt[0])][Integer.parseInt(sgeteilt[1])]);
        }
        shotSpeicher.clear();  //Leere die Liste fuer das naechste Feld

        boolean justShots = true; //Zeigt an, ob Schiff nur aus Shots besteht -> versenkt

        //Horizentale Suche in OPBOARD nach Schiffen im Array
        for (int i = 0; i < opBoard.length; i++) {
            for (int j = 0; j < opBoard.length; j++) {

                if (opBoard[i][j] != null && (opBoard[i][j].equals("S") || opBoard[i][j].equals("X") || opBoard[i][j].equals("V"))) {
                    esCounter++;

                    if (opBoard[i][j].equals("S")) justShots = false;

                    //Schiff zuende
                    if (j + 1 >= opBoard.length || j + 1 < opBoard.length && (opBoard[i][j + 1] == null || !(opBoard[i][j + 1].equals("S") || opBoard[i][j + 1].equals("X") || opBoard[i][j + 1].equals("V")))) {

                        //Schiff hat mindestens groesse 2 (Groesse kleiner 2 bedeutet, dass das Schiff vertikal ist)
                        if (esCounter >= 2) {

                            //Teste ob Schiff entdeckt -> alle Elemente sind shots u. links und rechts ist wasser
                            if (justShots && (j + 1 >= opBoard.length || (opBoard[i][j + 1] != null && opBoard[i][j + 1].equals("w"))) && (j - esCounter < 0 || (opBoard[i][j - esCounter] != null && opBoard[i][j - esCounter].equals("w")))) {
                                opcreateNewShip(esCounter, (j - (esCounter - 1)), i, false);
                            }

                        }

                        justShots = true; //justShots zuruecksetzen
                        esCounter = 0; //Zaehler zuruecksetzen
                    }

                }

                //if a shot was there
                if (opBoard[i][j] != null && (opBoard[i][j].equals("X") || opBoard[i][j].equals("V"))) {
                    shotSpeicher.add(i + "," + j);  //Speichere die Informationen der Schuesse in eine ArrayList um sie am Ende der beiden For-Schleifen zu zeichnen
                }

                //if a miss was there
                if (opBoard[i][j] != null && opBoard[i][j].equals("w")) {
                    myshot(String.valueOf(j), String.valueOf(i), opBoard[i][j]);
                }

                //wenn es nur ein S am Rand ist (vertikales schiff ist)
                if (esCounter <= 1 && j + 2 > opBoard.length)
                    esCounter = 0;

                // wenn es nur ein S ist (vertikales Schiff ist)
                if (esCounter <= 1 && j + 2 < opBoard.length && (opBoard[i][j + 1] == null || !(opBoard[i][j + 1].equals("S") || opBoard[i][j + 1].equals("X") || opBoard[i][j + 1].equals("V"))))
                    esCounter = 0;

            }
        }
        //Zeichne die Schuesse ins Feld ein
        for (String s : shotSpeicher) {
            String[] sgeteilt = s.split(",");
            myshot(sgeteilt[1], sgeteilt[0], "X");
        }
        shotSpeicher.clear();  //Leere die Liste

        // Vertikale Suche in OPBOARD nach Schiffen
        for (int i = 0; i < opBoard.length; i++) {
            for (int j = 0; j < opBoard.length; j++) {

                if (opBoard[j][i] != null && (opBoard[j][i].equals("S") || opBoard[j][i].equals("X") || opBoard[j][i].equals("V"))) {
                    esCounter++;

                    if (opBoard[j][i].equals("S")) justShots = false;

                    //Schiff zuende
                    if (j + 1 >= opBoard.length || j + 1 < opBoard.length && (opBoard[j + 1][i] == null || !(opBoard[j + 1][i].equals("S") || opBoard[j + 1][i].equals("X") || opBoard[j + 1][i].equals("V")))) {

                        //Schiff hat mindestens groesse 2 (Groesse kleiner 2 bedeutet, dass das Schiff vertikal ist)
                        if (esCounter >= 2) {

                            //Teste ob Schiff entdeckt -> alle Elemente sind shots u. links und rechts ist wasser
                            if (justShots && (j + 1 >= opBoard.length || (opBoard[j + 1][i] != null && opBoard[j + 1][i].equals("w"))) && (j - esCounter < 0 || (opBoard[j - esCounter][i] != null && opBoard[j - esCounter][i].equals("w")))) {
                                opcreateNewShip(esCounter, (i + 1), (j - (esCounter - 1)), true);
                            }

                        }

                        justShots = true; //justShots zuruecksetzen
                        esCounter = 0; //Zaehler zuruecksetzen
                    }

                }

                //if a shot was there
                if (opBoard[j][i] != null && (opBoard[j][i].equals("X") || opBoard[j][i].equals("V"))) {
                    shotSpeicher.add(i + "," + j);  //Speichere die Informationen der Schuesse in eine ArrayList um sie am Ende der beiden For-Schleifen zu zeichnen
                }

                //wenn es nur ein S am Rand ist (vertikales schiff ist)
                if (esCounter <= 1 && j + 2 > opBoard.length)
                    esCounter = 0;

                // wenn es nur ein S ist (vertikales Schiff ist)
                if (esCounter <= 1 && j + 2 < opBoard.length && (opBoard[j + 1][i] == null || !(opBoard[j + 1][i].equals("S") || opBoard[j + 1][i].equals("X") || opBoard[j + 1][i].equals("V"))))
                    esCounter = 0;

            }
        }
        //Zeichne die Schuesse ins Feld ein
        for (String s : shotSpeicher) {
            String[] sgeteilt = s.split(",");
            myshot(sgeteilt[0], sgeteilt[1], "X");
        }
        shotSpeicher.clear();  //Leere die Liste

    }

    /**
     * Schiffe werden erzeugt und platziert.
     *
     * @param shipsize
     * @param x
     * @param y
     * @return
     */
    private ImageView mycreateNewShip(int shipsize, int x, int y) {
        ImageView newShip = chooseShip(shipsize);
        paneMeinSpielfeld.getChildren().add(newShip);
        setShipSize(newShip, paneMeinSpielfeld.getPrefHeight() / size1);

        double hboxlayoutX = paneMeinSpielfeld.getLayoutX();
        double hboxlayoutY = paneMeinSpielfeld.getLayoutY();
        double shipTargetXScene = x * (paneMeinSpielfeld.getPrefWidth() / size1) + hboxlayoutX;
        double shipTargetYScene = y * (paneMeinSpielfeld.getPrefHeight() / size1) + hboxlayoutY;

        if (roated) {
            newShip.getTransforms().add(new Rotate(90, 0, 0));
        }

        Point2D point = newShip.sceneToLocal(shipTargetXScene, shipTargetYScene);
        newShip.getTransforms().add(new Translate(point.getX(), point.getY()));

        return newShip;
    }

    /**
     * Schiffe des Gegners werden erzeugt und platziert.
     *
     * @param shipsize
     * @param x
     * @param y
     * @param rotated
     * @return
     */
    private ImageView opcreateNewShip(int shipsize, int x, int y, boolean rotated) {

        ImageView newShip = chooseShip(shipsize);
        setShipSize(newShip, fieldsize);
        hboxgridpane1.getChildren().add(newShip);

        double hboxlayoutX = hboxgridpane1.getLayoutX();
        double hboxlayoutY = hboxgridpane1.getLayoutY();
        double shipTargetXScene = x * fieldsize + hboxlayoutX;
        double shipTargetYScene = y * fieldsize + hboxlayoutY;

        if (rotated) {
            newShip.getTransforms().add(new Rotate(90, 0, 0));
        }

        Point2D point = newShip.sceneToLocal(shipTargetXScene, shipTargetYScene);
        newShip.getTransforms().add(new Translate(point.getX(), point.getY()));

        return newShip;
    }


    /**
     * GUI Threading.
     */
    public class WaitForOpponentsMoveService extends Service<Boolean> {
        boolean succeeded = false;
        boolean gameOVer = false;


        private WaitForOpponentsMoveService() {
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded) {
                        arrayToGUI(Launcher.player.myBoard, Launcher.player.opBoard);
                        returnToGame();
                    } else if (gameOVer)
                        changeScene("Win_Lose", gridpane1);
                }
            });
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    try {
                        if (!Launcher.ai) {  //Wenn ein echter Spieler spielt
                            ((Player) (Launcher.player)).nextTurn(((Player) (Launcher.player)).aim);
                        } else ((Opponent_AI) (Launcher.player)).fire();//Spiel KI gegen KI starten
                        succeeded = true;
                    } catch (IOException e) {
                        System.err.println("Netzwerkfehler!");
                    } catch (GameOverException e) {
                        gameOVer = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return succeeded;
                }
            };

        }
    }

    /**
     * GUI Threading
     */
    public class WaitForShotResultService extends Service<Boolean> {
        boolean succeeded = false;
        boolean gameOVer = false;
        int x;
        int y;

        private WaitForShotResultService(String x, String y) {
            this.x = Integer.parseInt(x);
            this.y = Integer.parseInt(y);
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded) {
                        new WaitForOpponentsMoveService().start();
                        arrayToGUI(Launcher.player.myBoard, Launcher.player.opBoard);
                    } else if (gameOVer)
                        changeScene("Win_Lose", gridpane1);
                }
            });
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    try {
                        if (!Launcher.ai)  //Wenn ein echter Spieler spielt
                            ((Player) (Launcher.player)).startGame(x, y);
                        else ((Opponent_AI) (Launcher.player)).fire();//Spiel KI gegen KI starten
                        succeeded = true;
                    } catch (IOException e) {
                        System.err.println("Netzwerkfehler!");
                    } catch (GameOverException e) {
                        gameOVer = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return succeeded;
                }
            };

        }
    }

    /**
     * GUI Threading
     */
    public class ServerBeginningWait extends Service<Boolean> {
        boolean succeeded = false;

        private ServerBeginningWait() {

            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded) {
                        returnToGame();
                    }
                }
            });
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    try {
                        Launcher.ServerBeginningWait();
                        succeeded = true;
                    } catch (IOException e) {
                        System.err.println("Netzwerkfehler!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return succeeded;
                }
            };

        }
    }

    //Oponnents Turn -----------------------------------//

    /**
     * GUI darstellungen wenn die KI dran ist bzw Gegner.
     *
     * @param event
     */
    public void switch_turn(Event event) {
        Label_Opnnent_Shot.setVisible(true);
        saveButton.setDisable(true);
        transition_Label();
        gridpane1.setDisable(true);
    }

    boolean transitionSet = false;

    /**
     * GUI darstellungen.
     */
    public void transition_Label() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), Label_Opnnent_Shot);
        if (!transitionSet) {
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);

        } else {
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.0);
        }
        transitionSet = !transitionSet;
        fadeTransition.play();
    }

    /**
     * GUI darstellungen
     */
    public void returnToGame() {
        saveButton.setDisable(false);
        if (!flag1 && !(Launcher.type.equals("Single Player"))) {
            eigenesSpielfeld();
        }
        transition_Label();
        Label_Opnnent_Shot.setVisible(false);
        gridpane1.setDisable(false);
        new WaitForAnimationToComplete(510).start();
        if (Launcher.ai)//Wenn KI gegen KI
            new WaitForShotResultService("-2", "-2").start();
    }

    /**
     * GUI Threading
     */
    public class WaitForAnimationToComplete extends Service<Boolean> {
        public int anitmationDuration;

        private WaitForAnimationToComplete(int animationDuration) {
            this.anitmationDuration = animationDuration;
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    arrayToGUI(Launcher.player.myBoard, Launcher.player.opBoard);
                }
            });

        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {

                    while (timelineAnimationRunning || pathTransitionAnimationRunning || pathTransitionAnimationRunning1 || pathTransitionAnimationRunning2) {
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    return true;
                }
            };

        }
    }

    // >>>>>>>>>>>>>>>>> Removes the HUD by pressing the Button  if it ist necessary <<<<<<<<<<<<<<<<<<<<<<
    @FXML
    ImageView DisHUD;
    boolean HUD_disabled = false;

    /**
     * GUI Animation.
     */
    public void DisHUD() {
        timelineAnimationRunning = true;
        pathTransitionAnimationRunning = true;
        pathTransitionAnimationRunning2 = true;
        Timeline timeline = new Timeline();
        Path path = new Path();
        Path path1 = new Path();
        PathTransition pathTransition = new PathTransition();
        PathTransition pathTransition1 = new PathTransition();
        PathTransition pathTransition2 = new PathTransition();

        if (!HUD_disabled) {  // Initialsized with FALSE, TRUE when the Button(above the HUD) was Pressed

            path.getElements().add(new MoveTo(200, 200)); //  Point(Start)
            path.getElements().add(new LineTo(600, 200));     //Point (destination)
            pathTransition.setDuration(Duration.millis(300));

            path1.getElements().add(new MoveTo(30, 30));    //Y:200  // Animation for the Button
            path1.getElements().add(new LineTo(200, 30));      //Y:200
            pathTransition2.setNode(DisHUD);
            pathTransition2.setPath(path1);
            pathTransition2.setInterpolator(Interpolator.EASE_BOTH);


            pathTransition1.setNode(paneMeinSpielfeld);
            pathTransition1.setPath(path);
            pathTransition1.setInterpolator(Interpolator.EASE_BOTH);
            // Musst ich doppelt machen keine Ahunng warum...
            pathTransition.setNode(meinSpielfeld);
            pathTransition.setPath(path);
            pathTransition.setInterpolator(Interpolator.EASE_BOTH);


            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(0.5),
                    new KeyValue(DisHUD.rotateProperty(), 0)
            ));


        } else { // all reverse

            path.getElements().add(new MoveTo(600, 200)); //  Point(Start)
            path.getElements().add(new LineTo(200, 200));     //Point (destination)
            pathTransition.setDuration(Duration.millis(300));

            path1.getElements().add(new MoveTo(200, 30));    //Y:200   // Animation for the Button
            path1.getElements().add(new LineTo(30, 30));      //Y:200
            pathTransition2.setNode(DisHUD);
            pathTransition2.setPath(path1);
            pathTransition2.setInterpolator(Interpolator.EASE_BOTH);

            pathTransition.setNode(meinSpielfeld);
            pathTransition.setPath(path);
            pathTransition.setInterpolator(Interpolator.EASE_BOTH);

            pathTransition1.setNode(paneMeinSpielfeld);
            pathTransition1.setPath(path);
            pathTransition1.setInterpolator(Interpolator.EASE_BOTH);


            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.seconds(0.5),
                    new KeyValue(DisHUD.rotateProperty(), 180)
            ));
        }
        pathTransition.setOnFinished((e -> pathTransitionAnimationRunning = false));
        pathTransition1.setOnFinished((e -> pathTransitionAnimationRunning1 = false));
        pathTransition2.setOnFinished(e -> pathTransitionAnimationRunning2 = false);
        pathTransition.play();
        pathTransition1.play();
        pathTransition2.play();
        HUD_disabled = !HUD_disabled;
        timeline.setOnFinished((e -> timelineAnimationRunning = false));
        timeline.play();
    }


}