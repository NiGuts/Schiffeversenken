package battleships.GUI;

import battleships.logic.*;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * Controller für den Einzelspieler/Zweispieler/KIgegenKI-Modus.
 * Manche Scenen teilen sich diese Klasse weil vieles gleich abläuft.
 */
public class ControllerPlayer1 extends SuperController implements Initializable {

    private int size;
    private double fieldsize;
    private double orgSceneX;
    private double orgSceneY;
    private double orgTranslateX;
    private double orgTranslateY;
    private int zahl;
    private int[] shipSizes;
    private ArrayList<String> shipIds = new ArrayList<String>();
    private boolean iAmReady = false; //gibt an, ob Spieler schon ready gedrückt hat
    private boolean waitingForConfirmed = false;  //ob Spieler gerade auf Gegner wartet
    private boolean opIsReady = false;
    private String[][] oldTable; //aktuelles Spielfeld
    private ImageView latestShip;//Schiff dass zuletzt platziert wurde
    private Rectangle boardNotOk = new Rectangle(); //Rechteck, welches die Schiffe überdeckt, wenn das Feld ein ungültiges Schiff enthält: Der User soll erst wieder neue Schiffe holen können, wenn das Feld gültig ist
    private boolean roated;
    private boolean allreadyRandom = false; //gibt an ob Random schonmal gedrückt wurde
    Scene scene;
    Stage stage;

    public TextField field;
    public ImageView backreck; //Referenz auf das schwarze Rechteck wo man die Feldgröße einstellen kann
    public VBox vbox;  //Referenz auf die darüberliegende VBOX mit Textfield und Buttons
    public GridPane gridpane;//Referenz auf die gegnerische, große Gridpane
    public AnchorPane anchorPane;
    public ImageView _5ership0;  //Referenz auf das erste 5er Schiff, welches anfangs immer angezeigt wird
    public ImageView _4ership0;  //Referenz auf das erste 4er Schiff, welches anfangs immer angezeigt wird
    public ImageView _3ership0;  //Referenz auf das erste 3er Schiff, welches anfangs immer angezeigt wird
    public ImageView _2ership0;  //Referenz auf das erste 2er Schiff, welches anfangs immer angezeigt wird
    public Label label5erShips;
    public Label label4erShips;
    public Label label3erShips;
    public Label label2erShips;
    public VBox waitfor;
    public Rectangle waitforreck;
    public Button readyButton;
    public Button randomButton;

    /**
     * Initialisiert verschiedene Elemente.
     *
     * @param arg0
     * @param arg1
     */
    @FXML
    public void initialize(URL arg0, ResourceBundle arg1) {
        shipIds.clear();
        shipIds.add("5erSchiff0");
        shipIds.add("4erSchiff0");
        shipIds.add("3erSchiff0");
        shipIds.add("2erSchiff0");
        if (Launcher.type != null && Launcher.type.equals("Client")) {  //Wenn als Client gespielt wird, soll das schwarze Rechteck mit dem Textfeld nicht angezeigt werden, da der Server die Größe festlegt. In allen anderen Fällen schon
            backreck.setVisible(false); //Rechteck auf Player1 wird entfernt
            vbox.setVisible(false); //Vbox mit Knöpfe und Textfeld werden entfernt
            settable(null);
        }
    }

    /**
     * Entfernt nicht erlaubte Schiffe bei bestimmten Tabellengrößen.
     */
    public void removeIllegalShips() {
        if (shipSizes[0] == 0) { //Wenn es für die jetzige Feldgröße keine 5er Schiffe geben darf
            shipIds.remove(0);  //Id des 5erSchiffs aus der Liste löschen
            _5ership0.setVisible(false); //Indem man das 5er Schiff auf invisible setzt, wird es "gelöscht"
        }
        if (shipSizes[1] == 0) { //Wenn es für die jetzige Feldgröße keine 4er Schiffe geben darf
            shipIds.remove(0);  //Id des 4erSchiffs aus der Liste löschen, da das 5er Schiff schon removed wurde "rutscht" das 4er-Schiff auf die Position 0 in der ArrayList. Deswegen wird index 0 entfernt
            _4ership0.setVisible(false); //Indem man das 4er Schiff auf invisible setzt, wird es "gelöscht"
        }
    }

    public Button LoadGame;

    /**
     * Wechselt zu Menu zurück.
     *
     * @param event
     */
    public void back(MouseEvent event) {
        if (Launcher.opponent instanceof Op_Player) { //Wenn imm Mehrspielermodus oder KI gegen KI
            ((Op_Player) Launcher.opponent).closeSocket();
        }
        changeScene("Menu", event);
    }

    /**
     * Ready Button, setzt die Schiffe endgültig in die Tabelle.
     *
     * @param event
     * @throws IOException Wird geworfen wenn ein Netzwerk fehler auftritt.
     */
    public void ready(ActionEvent event) throws IOException {
        if (!allreadyRandom) {
            SuperController.myBoard = setArray();
            Launcher.player.cleanUp(SuperController.myBoard);  //Wasser entfernen
        }
        super.setTableSize(size, fieldsize);
        iAmReady = true;
        if (Launcher.player instanceof Player) {//Wenn nicht KI gegen KI
            if (!allreadyRandom) ((Player) Launcher.player).setupBoard(SuperController.myBoard);
            if (Launcher.type.equals("Server") || (Launcher.type.equals("Client"))) { //Der Spieler schickt confirmed und wartet PARALLEL auf confirmed
                if (Launcher.type.equals("Client")) { //Der Server wartet schon vorher auf den Gegner
                    Launcher.opponent.sendReady();
                    new WaitForConfirmationService().start();
                }
                if (Launcher.type.equals("Server") && opIsReady) {
                    Launcher.opponent.sendReady();
                    changeScene("Spiel", gridpane);
                }

                waitfor.setVisible(true);
                waitforreck.setVisible(true);
                waitforreck.toFront();
                waitfor.toFront();

            } else if (Launcher.type.equals("Single Player")) {     //Lokales Spiel gegen KI
                if (!allreadyRandom) Launcher.player.myBoard = setArray();
                Launcher.player.cleanUp(Launcher.player.myBoard);  //Wasser entfernen
                SuperController.myBoard = Launcher.player.myBoard;
                ((Player) Launcher.player).setupBoard(SuperController.myBoard);
                changeScene("Spiel", event);
            }

        }

    }

    /**
     * Prüft die eingebene Feldgröße, ob sie gültig ist.
     *
     * @param keyEvent
     */
    public void inputSize(KeyEvent keyEvent) {    //Event triggered when something is inserted into TextField

        boolean isTextRightNumber = checkSize(field.getText()); //Check if Text in Textfield has right format

        if (!isTextRightNumber) { //If Text in TextField does not have right format -> paint Border red
            field.setStyle("-fx-border-color: red ; -fx-border-width: 3px ;");
        } else { //Otherwise -> Right format -> Set Border Color to default
            field.setStyle("-fx-border-width: 0px ;");
        }
    }

    /**
     * Wird in der Methode "inputSize" aufgerufen, und checkt ob die eingegeben Feldgröße korrekt ist.
     *
     * @param number Ist die eingegebene Zahl.
     * @return
     */
    private boolean checkSize(String number) {
        int numberint = 0;

        try {
            numberint = Integer.parseInt(number); //Try to cast String to int
        } catch (Exception e) {
            return false;
        }

        if (numberint < 5 || numberint > 30) return false;

        return true;
    }

    /**
     * Generiert das Spielfeld.
     *
     * @param event
     */
    public void settable(ActionEvent event) {

      if (!Launcher.ai&&Launcher.type.equals("Server")){ new WaitForConfirmationService().start();}
        if (!checkSize(field.getText())) return; //Return if Number in TextField does NOT have right format
        String textinhalt;


        //------ initialze property "gedreht" of the ships with id 0
        _5ership0.getProperties().put("gedreht", false);
        _4ership0.getProperties().put("gedreht", false);
        _3ership0.getProperties().put("gedreht", false);
        _2ership0.getProperties().put("gedreht", false);
        //------ END initialization of "gedreht"

        if (Launcher.type == null || !Launcher.type.equals("Client"))  //Wenn nicht als Client gespielt wird, kann die gewünschte Feldgröße vom Server ausgelesen werden, anderfalls kommt sie vom Server
            textinhalt = field.getText();  // Reads Text from 'field'
        else {
            textinhalt = GameObject.size + "";
        }// Das vom Server empfangene Size
        size = Integer.parseInt(textinhalt);  // Sets the 'size' attribute with the value from

        fieldsize = (int) (950.0 / size);

        if ((Launcher.type).equals("Single Player"))
            Launcher.singlePlayer(size + "");
        if (Launcher.type != null && !Launcher.type.equals("Client")) { //Wenn als Server gespielt wird, muss die size geschickt werden
            if (!Launcher.type.equals("Single Player")) //In der setSize Methode wird u.a. das opBoard auf null gesetzt. Das soll beim lokalen Spiel gegen die KI nicht geschehen
                Launcher.player.setSize(size + "");
            SuperController.setTableSize(size, fieldsize);
            Launcher.opponent.sendFieldsize(); //Größe schicken
            if (Launcher.ai) { //Brett setzen für Spiel gegen KI
                SuperController.myBoard = Launcher.player.myBoard;
            }

        }

        for (int i = 0; i < size; i++) {
            ColumnConstraints colum = new ColumnConstraints();
            gridpane.getColumnConstraints().add(colum);
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(fieldsize + 1);
            gridpane.getRowConstraints().add(row);
            colum.setPrefWidth(fieldsize + 1);

        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Rectangle rectangle = new Rectangle();
                rectangle.setId("Rect" + i + "-" + j);  //Das vorherige 'i+j' ist nicht eindeutig: ist 112 nun (11,2) oder (1,12)?
                rectangle.setHeight(fieldsize - 2);
                rectangle.setWidth(fieldsize - 2);
                rectangle.setFill(Color.ROYALBLUE);
                rectangle.setStyle("-fx-arc-width: 20; -fx-arc-height: 20");
                rectangle.setStroke(Color.hsb(0, 0, 0.3));
                gridpane.add(rectangle, i, j);
                rectangle.setStrokeWidth(0);
            }
        }
        shipSizes = Arrays.copyOf(GameObject.shipSizes, GameObject.shipSizes.length);
        updateShipCounters();
        removeIllegalShips();
        if (!Launcher.ai) { //Wenn als "echter Spieler" gespielt wird, soll das backreck verschwinden, damit der Spieler die Schiffe positionieren kann.
            backreck.setVisible(false); //Rechteck auf Player1 wird entfernt
            vbox.setVisible(false); //Vbox mit Knöpfe und Textfeld werden entfernt
        } else {
            if (Launcher.type.equals("Client")) {//Wenn KI spielt, beginnt das Spiel direkt
                try {
                    Launcher.opponent.sendReady();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("Netzwerkfehler!");
                }
            }
            new WaitForConfirmationService().start();
        }
    }

    /**
     * Das Feld auf welches gehovert wird, wird angezeigt.
     */
    public EventHandler hover = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            Rectangle rectangle = (Rectangle) event.getSource();

            if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                rectangle.setStroke(Color.RED);
                rectangle.setStrokeWidth(2);
            } else {
                rectangle.setStroke(Color.hsb(0, 0, 0.3));
                rectangle.setStrokeWidth(0);
            }

        }
    };

    /**
     * Erhöht die angezeigte Zahl im Text Feld um eins, bis maximum 30.
     *
     * @param event
     */
    public void plus(ActionEvent event) {
        Button button = (Button) event.getSource();
        Scene scene1 = ((Node) event.getSource()).getScene();
        String s = field.getText();

        //Try to Parse Text in TextField to Integer
        try {
            zahl = Integer.parseInt(s);
        } catch (Exception e) { //Throws if Text in TextField contains not parsable Elements (not numbers)
            return;
        }

        zahl++;
        if (zahl >= 30)
            zahl = 30;
        field.setText(String.valueOf(zahl));

        boolean isTextRightNumber = checkSize(zahl + ""); //Check if new number has right format

        if (!isTextRightNumber) { //If Text in TextField does not have right format -> paint Border red
            field.setStyle("-fx-border-color: red ; -fx-border-width: 1px ;");
        } else { //Otherwise -> Right format -> Set Border Color to default
            field.setStyle("-fx-border-width: 0px ;"); //hide border
        }
    }

    /**
     * Verringert die angezeigte Zahl um eins, bis minimum 5.
     *
     * @param event
     */
    public void minus(ActionEvent event) {

        Button button = (Button) event.getSource();
        Scene scene1 = ((Node) event.getSource()).getScene();

        String s = field.getText();

        //Try to Parse Text in TextField to Integer
        try {
            zahl = Integer.parseInt(s);
        } catch (Exception e) { //Throws if Text in TextField contains not parsable Elements (not numbers)
            return;
        }

        zahl--;
        if (zahl <= 5)
            zahl = 5;
        field.setText(String.valueOf(zahl));

        boolean isTextRightNumber = checkSize(zahl + ""); //Check if new number has right format

        if (!isTextRightNumber) { //If Text in TextField does not have right format -> paint Border red
            field.setStyle("-fx-border-color: red ; -fx-border-width: 1px ;");
        } else { //Otherwise -> Right format -> Set Border Color to default
            field.setStyle("-fx-border-width: 0px ;");
        }
    }

    int idCounter = 0;

    /**
     * Bewältigt das Drag and Drop.
     *
     * @param t Das Event welches auf dem ausgewählten Schiff vollführt wird.
     */
    public void handle(MouseEvent t) {
        readyButton.setDisable(true);
        scene = ((Node) t.getSource()).getScene();
        ImageView oldship = ((ImageView) t.getSource());
        if (oldship.localToScene(0, 0).getX() < gridpane.getLayoutX()) { //Wenn das Schiff nicht schon in der Gridpane ist
            //Der ShipSizes-Array ist wie folgt aufgebaut: Anzahl 5er an Index 0, Anzahl 4er an Index 1...
            //Mit oldship.getId().substring(0, 1) erhalten wir den ersten Buchstaben der ID, also die Größe des
            //zu kopierenden Schiffes. Daraus wird dann der Index im shipSizes Array berechnet, welcher der Schifflänge entspricht
            int requiredIndex = 5 - Integer.parseInt(oldship.getId().substring(0, 1));
            shipSizes[requiredIndex] -= 1; //Der Spieler hat ein neues Schiff erzeugt, deshalb hat er jetzt eins weniger zur Verfügung
            updateShipCounters();
            if (shipSizes[requiredIndex] > 0) {  //Überprüft, ob der Spieler schon alle Schiffe gesetzt hat
                ImageView newship = new ImageView();
                newship.setImage(oldship.getImage());
                newship.setLayoutX(oldship.getLayoutX());
                newship.setLayoutY(oldship.getLayoutY());
                newship.setVisible(true);

                //Id wird für die neuen Schiffe gesetzt
                idCounter++;
                String id = oldship.getId().substring(0, 9);

                //Schiff Id muss noch gesetzt werden
                newship.setId(id + idCounter);
                shipIds.add(newship.getId());

                //Höhe vom neuen schiff wird gesetzt
                newship.setFitHeight(oldship.getFitHeight());
                newship.setFitWidth(oldship.getFitWidth());


                //Das neue Schiff bekommt die "Methoden" von der FXML
                newship.setOnMousePressed(this::handle);
                newship.setOnMouseDragged(this::dragship);
                newship.setOnContextMenuRequested(this::rotated);
                newship.setOnMouseReleased(this::drop);
                newship.setCursor(Cursor.MOVE);

                //Shiff wird zur Scene hinzugefügt
                AnchorPane anchorPane = (AnchorPane) scene.lookup("#anchor");
                anchorPane.getChildren().add(newship);
                newship.getProperties().put("gedreht", false);  //adds the property "gedreht" with the initial value "false" to the Map

            }
            //die Methode wird aufgerufen um die Schiffe auf die Feldgroesse gesetzt
            setShipSize(((ImageView) t.getSource()), fieldsize + 1);
        }
        orgSceneX = t.getSceneX();
        orgSceneY = t.getSceneY();
        orgTranslateX = ((ImageView) (t.getSource())).getTranslateX();
        orgTranslateY = ((ImageView) (t.getSource())).getTranslateY();
    }

    double rotation;

    /**
     * Rotiert die Schiffe beim platzieren.
     *
     * @param event
     */
    public void rotated(Event event) {
        if (((ImageView) (event.getSource())).localToScene(0, 0).getX() >= gridpane.getLayoutX()) {

            ImageView ship = (ImageView) event.getSource();

            double shipcenterX = ship.getFitWidth() / 2;
            double shipcenterY = ship.getFitHeight() / 2;

            if (!(boolean) ship.getProperties().get("gedreht")) {  //gets the value of the property "gedreht"
                rotation = 90;
                ship.getProperties().put("gedreht", true);  //sets the value of the property "gedreht" to "true"
            } else {
                rotation = -90;
                ship.getProperties().put("gedreht", false); //sets the value of the property "gedreht" to "false"
            }

            ship.getTransforms().add(new Rotate(rotation, shipcenterX, shipcenterY));
            drop(event);
        }
    }

    /**
     * Dropt das augewählte Schiff auf das gewünschte Feld.
     *
     * @param event
     */
    public void drop(Event event) {
        drop((Node) (event.getSource()));
    }

    public void drop(Node node) {
        oldTable = new String[Launcher.player.myBoard.length][Launcher.player.myBoard.length];
        for (int i = 0; i < Launcher.player.myBoard.length; i++) {
            for (int j = 0; j < Launcher.player.myBoard.length; j++) {
                oldTable[i][j] = Launcher.player.myBoard[i][j];
            }
        }
        ImageView ship = (ImageView) node; //'ship' ist das Objekt, welches das Event ausloest

        Point2D gPaneScenePos = gridpane.localToScene(0, 0);  //position of the GridPane in the Scene
        Point2D shipScenePos = ship.localToScene(0, 0);  //position of the GridPane in the Scene
        Point2D shipGPanePos = gridpane.sceneToLocal(shipScenePos.getX() + ((fieldsize + 1) / 2), shipScenePos.getY() + ((fieldsize + 1) / 2)); //position of the ship in the GridPane
        //add (fieldsize+1)/2 to use the middle of the
        //left field as the snapping point
        double gPaneDimension = gridpane.getWidth();  //Width of the GridPane (= Height of the GridPane)
        randomButton.setDisable(true);
        int numberOfFields = (int) (gPaneDimension / (fieldsize + 1));  //number of Fields in each Row/Col

        int shipInXField = (int) (Math.floor(shipGPanePos.getX() / (fieldsize + 1)));  //number of the field in the X dimension the ship is located in (starting at 0)
        int shipInYField = (int) (Math.floor(shipGPanePos.getY() / (fieldsize + 1)));  //number of the field in the Y dimension the ship is located in (starting at 0)

        // the difference of the Ship to the designated position

        Point2D shipToPositionDifference = ship.sceneToLocal(gPaneScenePos.getX() + shipInXField * (fieldsize + 1), gPaneScenePos.getY() + shipInYField * (fieldsize + 1));

        if ((boolean) ship.getProperties().get("gedreht")) {
            shipInXField = shipInXField - 1;
        }

        int shipSize = Character.getNumericValue(ship.getId().charAt(0));  // extract the ship size from shipID (e.g. 4erSchiff0 -> extract: 4)

        //check if ship is outside field
        if ((boolean) ship.getProperties().get("gedreht")) {
            if (shipInXField < 0 || shipInYField < 0 || shipInXField > numberOfFields - 1 || shipInYField + shipSize > numberOfFields) {
                removeShip(ship);
                return;
            }
        } else {
            if (shipInXField < 0 || shipInYField < 0 || shipInYField > numberOfFields - 1 || shipInXField + shipSize > numberOfFields) {
                removeShip(ship);
                return;
            }
        }
        latestShip = ship;
        ship.getTransforms().add(new Translate(shipToPositionDifference.getX(), shipToPositionDifference.getY()));
        setArray();
    }

    /**
     * Ein Schiff wird beim platzieren entfernt wenn es auserhalb der Feldes plaziert wird.
     *
     * @param ship
     */
    private void removeShip(ImageView ship) {
        randomButton.setDisable(false);
        anchorPane.getChildren().remove(ship);  //removes ship from the anchorPane
        shipIds.remove(ship.getId());//Löschen des Schiffes aus der Id-Liste
        int shipLength = Integer.parseInt(ship.getId().substring(0, 1));
        if (shipSizes[5 - shipLength] <= 0) {
            ImageView newship = new ImageView();
            newship.setImage(ship.getImage());
            newship.setLayoutX(ship.getLayoutX());
            newship.setLayoutY(ship.getLayoutY());
            newship.setVisible(true);
            newship.setId(ship.getId());
            shipIds.add(newship.getId());
            newship.setFitHeight(ship.getFitHeight());
            newship.setFitWidth(ship.getFitWidth());
            //Das neue Schiff bekommt die "Methoden" von der FXML
            newship.setOnMousePressed(this::handle);
            newship.setOnMouseDragged(this::dragship);
            newship.setOnContextMenuRequested(this::rotated);
            newship.setOnMouseReleased(this::drop);
            newship.setCursor(Cursor.MOVE);
            //Shiff wird zur Scene hinzugefügt
            anchorPane.getChildren().add(newship);
            newship.getProperties().put("gedreht", false);  //adds the property "gedreht" with the initial value "false" to the Map
        }
        shipSizes[5 - shipLength] += 1; //Wenn ein Schiff gelöscht wird, muss der counter der verfügbaren Schiffe erhöht werden (siehe Kommentar in handle())
        updateShipCounters();
        ship = null;
    }

    /**
     * Beim ziehen der Schiffe wird diese Methode aufgerufen.
     *
     * @param dragEvent Event auf dem Schiff, wenn es ausgewählt wird.
     */
    public void dragship(MouseEvent dragEvent) {
        ImageView ship = (ImageView) dragEvent.getSource();

        double offsetX = dragEvent.getSceneX() - orgSceneX;
        double offsetY = dragEvent.getSceneY() - orgSceneY;
        double newTranslateX = orgTranslateX + offsetX;
        double newTranslateY = orgTranslateY + offsetY;
        ((ImageView) (dragEvent.getSource())).setTranslateY(newTranslateY);
        ((ImageView) (dragEvent.getSource())).setTranslateX(newTranslateX);

    }

    /**
     * Schreibt die Schiffe in ein Stringarray.
     *
     * @return Array mit Schiffen
     */
    public String[][] setArray() {
        String[][] table = new String[size][size];
        for (int i = 0; i < Launcher.player.myBoard.length; i++) {
            for (int j = 0; j < Launcher.player.myBoard.length; j++) { //Spielfeld in der Logik und GuI resetten
                Launcher.player.myBoard[i][j] = null;
                String id = "#Rect" + i + "-" + j;
                ((Rectangle) gridpane.getScene().lookup(id)).setFill(Color.ROYALBLUE);
            }
        }
        return addToArray(table);  //Komplett neues Array mit Schiffen füllen
    }

    /**
     * Neue Schiffe werden dem existierenden Array table hinzugefügt
     *
     * @param table Tabelle mit hinzukommenden Schiffen.
     * @return
     */
    public String[][] addToArray(String[][] table) {  //Neue Schiffe werden dem existierenden Array table hinzugefügt
        ImageView ship;
        for (int i = 0; i < shipIds.size(); i++) {
            ship = (ImageView) scene.lookup("#" + shipIds.get(i));
            if (ship == null) break;  //breaks if no element with id "shipId" was found

            Point2D shipScenePos = ship.localToScene(0, 0);
            Point2D shipGPanePos = gridpane.sceneToLocal(shipScenePos.getX() + ((fieldsize + 1) / 2), shipScenePos.getY() + ((fieldsize + 1) / 2));
            int shipInXField = (int) (Math.floor(shipGPanePos.getX() / (fieldsize + 1)));
            int shipInYField = (int) (Math.floor(shipGPanePos.getY() / (fieldsize + 1)));

            if (((boolean) ship.getProperties().get("gedreht")))
                shipInXField--; //Provisorisch da die berchenung für gedrehte schiffe einen um eins zugroßen x wert liefert

            //Wenn die Schiffe Horizontal liegen
            if (shipInXField >= 0 && shipInYField >= 0 && !((boolean) ship.getProperties().get("gedreht"))) {

                for (int j = 0; j < Character.getNumericValue(ship.getId().charAt(0)); j++) {
                    table[shipInYField][shipInXField + j] = "S";
                }
                checkBoard(table, shipInYField, shipInXField, ship, false);
            }
            //wenn die Schiffe gedreht sind
            else if ((shipInXField >= 0 && shipInYField >= 0 && ((boolean) ship.getProperties().get("gedreht")))) {

                for (int j = 0; j < Character.getNumericValue(ship.getId().charAt(0)); j++) {
                    table[shipInYField + j][shipInXField] = "S";
                }
                checkBoard(table, shipInYField, shipInXField, ship, true);
            }
        }

        return table;
    }

    // should open the File window were you can choose the saved game :D
    final FileChooser fileChooser = new FileChooser();

    /**
     * Öffnet den FileChooser, um eine CSV Datei auszuwählen.
     *
     * @param event
     * @throws IOException Wird geworfen wenn das öffnen des FileChooser nicht funtkioniert.
     */
    public void FileChooser(ActionEvent event) throws IOException {

        fileChooser.setTitle("Pick the CSV :D");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Dateien", "*.CSV"));
        fileChooser.setInitialDirectory(new File("Schiffeversenken2.0/..").getAbsoluteFile());

        File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            String name = file.getName();
            if (Launcher.type.equals("Single Player")) {
                Launcher.singlePlayer("10");//lokales Spiel gegen KI, Größe wird später überschreiben
            }
            Launcher.opponent.sendLoad(Long.valueOf(name.split(".CSV")[0]));
            if (CSVmanager.load(file.getName()) && !Launcher.type.equals("Single Player")) //Wenn ich gespeichert habe, kommt jetzt der Gegner dran --> werde zum Server
                Launcher.type = "Server";
            else if (!Launcher.type.equals("Single Player"))//Wenn der Gegner gespeichert hat, komme ich dran --> werde zum Client
                Launcher.type = "Client";
            changeScene("Spiel", readyButton);
        }
    }

    /**
     * GUI Threading.
     */
    public class WaitForConfirmationService extends Service<Boolean> {
        boolean succeeded = false;

        private WaitForConfirmationService() {
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded) {
                        if (Launcher.ai) iAmReady = true;
                        if (Launcher.type.equals("Server") && !iAmReady)
                            OpponentReady();
                        else if (Launcher.type.equals("Client")) changeScene("Spiel", gridpane);
                        if (Launcher.type.equals("Server") && iAmReady) {
                            try {
                                Launcher.opponent.sendReady();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            ;
                            changeScene("Spiel", gridpane);
                        }

                    }
                }
            });
        }


        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    if ((Launcher.type.equals("Server")) || (Launcher.type.equals("Client") && iAmReady || Launcher.ai)) { //Server soll nicht zweimal auf confirmed warten
                        try {
                            waitingForConfirmed = true;
                            String[] newBufferEntry = Launcher.opponent.getBufferedElement(); //erwartet 'confirmed'
                            if (newBufferEntry[0].equals("confirmed")) {
                                succeeded = true;
                                opIsReady = true;
                            } else Launcher.opponent.abortMission("confirmed", newBufferEntry);
                        } catch (Exception e) {
                            System.err.println("Netzwerkfehler!");
                        }
                    } else if (!waitingForConfirmed || (iAmReady && waitingForConfirmed && opIsReady)) {
                        succeeded = true;
                    }
                    return succeeded;
                }
            };

        }
    }

    // -----------------------------------------------//


    //Beginn Handles the Label on the Scene Player1 next to the "Bereit" Button//
    @FXML
    Label OpponentReady;

    /**
     * Design technisch.
     */
    public void OpponentReady() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(700), OpponentReady);
        fadeTransition.setInterpolator(Interpolator.EASE_BOTH);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
    //End -----------------------------------------------------------

    /**
     * Aktualisiert die Schiff counter beim platzieren.
     */
    private void updateShipCounters() {
        label2erShips.setText(shipSizes[3] + ""); //Aktualisieren des zugehörigen Labels
        label3erShips.setText(shipSizes[2] + ""); //Aktualisieren des zugehörigen Labels
        label4erShips.setText(shipSizes[1] + ""); //Aktualisieren des zugehörigen Labels
        label5erShips.setText(shipSizes[0] + ""); //Aktualisieren des zugehörigen Labels
    }

    /**
     * Prüft die Gültigkeit der platzierten Schiffe
     *
     * @param table        Die Tabelle in der die Schiffe dirn sind.
     * @param shipInYField
     * @param shipInXField
     * @param ship         Das übergeben Schiff.
     * @param rotated      Ob das aktulle Schiff gedreht ist.
     */
    private void checkBoard(String[][] table, int shipInYField, int shipInXField, ImageView ship, boolean rotated) {
        Color c;
        if (!Launcher.player.setShips(table)) {
            c = Color.ORANGERED;
            ship = latestShip;
            boardNotOk.setHeight(1500);
            boardNotOk.setWidth(450);
            boardNotOk.setFill(Color.rgb(100, 40, 33, 0.5));
            if (!anchorPane.getChildren().contains(boardNotOk))
                anchorPane.getChildren().add(boardNotOk);
            Point2D shipScenePos = ship.localToScene(0, 0);
            Point2D shipGPanePos = gridpane.sceneToLocal(shipScenePos.getX() + ((fieldsize + 1) / 2), shipScenePos.getY() + ((fieldsize + 1) / 2));
            shipInXField = (int) (Math.floor(shipGPanePos.getX() / (fieldsize + 1)));
            shipInYField = (int) (Math.floor(shipGPanePos.getY() / (fieldsize + 1)));
            if (((boolean) ship.getProperties().get("gedreht"))) {
                shipInXField--; //Provisorisch da die berchenung für gedrehte schiffe einen um eins zugroßen x wert liefert
                rotated = true;
            }
        } else {
            int totalSAmount = 0; //Die Gesamtanzahl der "S" im Array. Damit wird geprüft, ob alle Schiffe gesetzt sind
            for (int i = 0; i < table.length; i++) {
                for (int j = 0; j < table.length; j++) {
                    if (table[i][j] != null && table[i][j].equals("S"))
                        totalSAmount++;
                }
            }
            if (totalSAmount == Launcher.player.lifes) readyButton.setDisable(false);
            if (anchorPane.getChildren().contains(boardNotOk))
                anchorPane.getChildren().remove(boardNotOk);
            c = Color.LAWNGREEN;
        }
        for (int _j = 0; _j < Character.getNumericValue(ship.getId().charAt(0)); _j++) {
            if (!rotated)
                ((Rectangle) gridpane.getScene().lookup("#Rect" + (shipInXField + _j) + "-" + shipInYField)).setFill(c);
            else
                ((Rectangle) gridpane.getScene().lookup("#Rect" + shipInXField + "-" + (shipInYField + _j))).setFill(c);

        }
    }


    public void placeShipsRandomly(ActionEvent e) {
        scene = ((Node) (e.getSource())).getScene();
        int z = shipIds.size();
        for (int i = 0; i < z; i++) {
            ImageView v = (ImageView) (scene.lookup("#" + shipIds.get(0)));
            shipIds.remove(v);
            if (v != null)
                removeShip(v);
        }
        Launcher.player.myBoard = new String[Launcher.player.myBoard.length][Launcher.player.myBoard.length]; //Spielfeld leeren
        String[][] myBoard = Launcher.player.myBoard;
        Launcher.player.setShipsRandomly();
        Viewer.drawBoard(myBoard, myBoard);
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
                    if (myBoard[i][j] != null && j <= myBoard.length - 1 && (myBoard[i][j].equals("S") || myBoard[i][j].equals("X")) && esCounter >= 2 && j == myBoard.length - 1) {
                        mycreateNewShip(esCounter, (j - (esCounter - 1)), i);
                        esCounter = 0;
                    }
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
        if (!allreadyRandom)
            allreadyRandom = true;
        Arrays.fill(shipSizes, 0);
        updateShipCounters();
        readyButton.setDisable(false);
        ((Node) (e.getSource())).setDisable(true);
        Launcher.player.cleanUp(myBoard);
        SuperController.myBoard = Launcher.player.myBoard;
    }

    private ImageView mycreateNewShip(int shipsize, int x, int y) {

        ImageView newShip = chooseShip(shipsize);
        newShip.toBack();
        anchorPane.getChildren().add(newShip);
        setShipSize(newShip, fieldsize);


        double hboxlayoutX = gridpane.getLayoutX();
        double hboxlayoutY = gridpane.getLayoutY();
        double shipTargetXScene = x * (fieldsize + 1) + hboxlayoutX;
        double shipTargetYScene = y * (fieldsize + 1) + hboxlayoutY;

        if (roated) {
            newShip.getTransforms().add(new Rotate(90, 0, 0));
        }
        Point2D point = newShip.sceneToLocal(shipTargetXScene, shipTargetYScene);
        newShip.getTransforms().add(new Translate(point.getX(), point.getY()));

        newShip.getProperties().put("gedreht", roated);
        scene = newShip.getScene();
        shipIds.add(newShip.getId());
        if (latestShip == null)
            latestShip = newShip;

        return newShip;
    }
}
