package battleships.GUI;

import battleships.logic.Launcher;
import battleships.logic.LoadGameException;
import battleships.logic.Op_Player;
import battleships.network.Server;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;

import java.net.ConnectException;

/**
 * Klasse ist für die Controller der PVP Scene zustädnig.
 */
public class ControllerPVP extends SuperController {

    String IP = "";
    TextField textField = null;
    @FXML
    Button HostButton;
    static Button keeper;  //In diesem statischen Element wird die Referenz auf den Host Button gespeichert
    static Scene scene;

    /**
     * Wechselt die Scene zu zurück Menu.
     *
     * @param mouseEvent
     */
    public void BackfromPlayertwo(MouseEvent mouseEvent) {

        changeScene("Menu", mouseEvent);
    }

    /**
     * Verbindungsaufbau zum Host.
     *
     * @param event
     */
    public void Connect(ActionEvent event) {
        keeper = HostButton;
        //Connect Funktion im Interface aufrufen um IP adresse weiter zu schicken
        //im try catch Block wenn vom Netzwerk bzw von der Logik eine IOException kommt dann soll der rand rot werden geht aber nur mit CSS
        scene = ((Node) event.getSource()).getScene();
        initializeIPTextField(event);
        if (isIPFormat(IP)) { //If 'IP' is correctly formatted

            if (IP.equalsIgnoreCase("localhorst")) {
                IP = "localhost";
            }

            ConnectWait(event);
            new LaunchClientService(/*textField.getText()*/IP).start();

        } else {
            // Player pressed the 'CONNECT'-button but the IP-adress is NOT correctly formatted
            // 'textField' is painted red
            textField.setStyle("-fx-border-color: rgb(164,28,23);" +
                    "-fx-border-width: 2pt;" +
                    "-fx-background-color: rgb(247,218,212)");
        }

    }

    /**
     * GUI darstellung für das verbinden.
     *
     * @param event
     */
    public void ConnectWait(ActionEvent event) {
        Scene scene = ((Node) (event.getSource())).getScene();
        Rectangle rec = (Rectangle) (scene.lookup("#WaitingRec"));
        HBox hbox = (HBox) (scene.lookup("#Connecting_hbox"));

        rec.setVisible(true);
        hbox.setVisible(true);
    }

    /**
     * Initialisiert das TextFeld der IP.
     *
     * @param event
     */
    private void initializeIPTextField(Event event) {
        if (textField == null) {
            textField = (TextField) ((Node) event.getSource()).getScene().lookup("#TextFieldforIP");
        }
    }

    /**
     * Checkt ob das richtige Format der IP eingegeben wurde.
     *
     * @param keyEvent
     */
    public void Textfeld(KeyEvent keyEvent) {
        initializeIPTextField(keyEvent);

        IP = ((TextField) (keyEvent.getSource())).getText();

        Boolean isIP = isIPFormat(IP);
        if (isIP) { //If 'IP' is formatted as an IP -> set border color = green
            textField.setStyle("-fx-border-color: green ; -fx-border-width: 3px ;");
        } else { //otherwise set border color = red
            textField.setStyle("-fx-border-color: red ; -fx-border-width: 3px ;");
        }

    }

    /**
     * Wird in "Textfeld" aufgerufen und checkt ob der Format der IP korrekt ist.
     *
     * @param ip Eingebene IP.
     * @return Gibt true, wenn das Format der IP korrekt ist sonst, false.
     */
    private boolean isIPFormat(String ip) { //Tests if the String contains a correctly formattet IPv4-Adress

        if (ip.startsWith(".") || ip.endsWith(".")) return false;

        String[] splitip = ip.split("\\."); //String is splitted around '.'

        //if String is equals to "localhost" -> return true
        if (splitip[0] != null && (splitip[0].equalsIgnoreCase("localhost") || splitip[0].equalsIgnoreCase("localhorst")))
            return true;

        if (splitip.length != 4) return false; //If there are more than 3 '.' -> return false;

        for (int i = 0; i < 4; i++) {

            try {
                int a = Integer.parseInt(splitip[i]); //Check if every splitted block only contains numbers -> Exception if otherwise
                if (a > 255) return false; //Chech if number in every block is less than 255
                if (splitip[i].length() > 3) return false; //If there are more than 3 numbers in every block
            } catch (Exception e) {
                return false;
            }

        }

        return true;
    }

    /**
     * GUI darstellungen.
     *
     * @param event
     * @throws Exception
     */
    public void HostButton(ActionEvent event) throws Exception {

        Scene scene = ((Node) event.getSource()).getScene();
        scene.lookup("#hboxwaiting").setVisible(true);
        scene.lookup("#WaitingRec").setVisible(true);
        TextField textField = (TextField) scene.lookup("#IPAdressHosting");

        textField.setOpacity(1.0);

        try {

            String s = Server.getIP();
            textField.setText(s);

        } catch (Exception exception) {

            textField.setText("FEHLGESCHLAGEN");

            System.err.println("FEHLGESCHLAGEN");

        }

        new LaunchServerService().start();
    }

    /**
     * Wechselt zur Scene für das Schiffe zu platzieren.
     */
    public void changeToPlayer1asServer() {
        changeScene("Player1", HostButton);
    }

    /**
     * GUI darstellungen für's Hosten.
     *
     * @param event
     */
    public void quit_hosting(ActionEvent event) {
        ((Op_Player) Launcher.opponent).closeSocket();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.lookup("#hboxwaiting").setVisible(false);
        scene.lookup("#WaitingRec").setVisible(false);
    }

    /**
     * GUI darstellungen für's Connecten.
     *
     * @param event
     */
    public void quit_connecting(ActionEvent event) {
        //Wenn imm Mehrspielermodus oder KI gegen KI
        ((Op_Player) Launcher.opponent).closeSocket();
        Scene scene = ((Node) event.getSource()).getScene();
        scene.lookup("#Connecting_hbox").setVisible(false);
        scene.lookup("#WaitingRec").setVisible(false);
    }

    /**
     * GUI Threading.
     */
    public class LaunchClientService extends Service<Boolean> {
        boolean succeeded;
        String ip;

        private LaunchClientService(String ip) {
            this.ip = ip;
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded && !Launcher.ai) {  //Wenn ein echter Spieler spielt, wechsel zur Schiffspositionierungs-scene
                        changeScene("Player1", keeper);
                    } else if (succeeded && Launcher.ai) {  //Wenn KI spielt, direkter Wechsel zum Spiel
                        SuperController.setTableSize(Launcher.player.size, 950 / Launcher.player.size);
                        SuperController.myBoard = Launcher.player.myBoard;//Brett des Clients setzen
                        changeScene("Spiel", keeper);
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
                        if (!Launcher.ai)//Wenn echter Spieler spielt
                            Launcher.playerAsClient(ip);
                        else Launcher.aiAsClient(ip);
                        succeeded = true;
                    } catch (ConnectException e) {
                        e.printStackTrace();
                        System.err.println("Unable to connect to given IP");
                        succeeded = false;
                    } catch (LoadGameException e) {
                        changeScene("Spiel", keeper);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return succeeded;
                }
            };

        }
    }


    public class LaunchServerService extends Service<Boolean> {
        boolean succeeded = false;

        private LaunchServerService() {
            setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    if (succeeded) {
                        changeToPlayer1asServer();
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
                        if (!Launcher.ai) { //Wenn ein echter Spieler spielt
                            Launcher.playerAsServer("10");//Die übergebene Größe ist nur provisiorisch, weil ich den Konstruktor leider so gebaut habe das man eine Größe übergeben muss. Sie wird in der nächsten Szene überschrieben
                            succeeded = true;
                        } else {
                            Launcher.aiAsServer("10"); //Die übergebene Größe ist nur provisiorisch, weil ich den Konstruktor leider so gebaut habe das man eine Größe übergeben muss. Sie wird in der nächsten Szene überschrieben
                            succeeded = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        succeeded = false;
                    }
                    return succeeded;
                }
            };

        }
    }

}
