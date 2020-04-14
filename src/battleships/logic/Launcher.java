package battleships.logic;
//import Logic from Logic

import battleships.network.Server;

import java.io.IOException;

/**
 *
 */
public class Launcher extends GameObject {

    public static GameObject player;
    public static GameObject opponent;
    public static String type; //Soll entweder "Server", "Client" oder "single Player" sein.
    public static boolean ai = false; // true, wenn man selber als KI spielt
    public static String result = "";

    /**
     * Main Methode zum Testen
     *
     * @param args übergebene Kommandozeilenparameter um Spielmodi zu testen
     * @throws Exception bei internem Fehler
     */
    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            if (args[0].equals("s")) {
                playerAsServer(args[1]);
            } else if (args[0].equals("c")) {
                playerAsClient(args[1]);
            } else if (args[0].equals("a")) {
                singlePlayer(args[1]);
            } else if (args[0].equals("aa")) {
                if (args[1].equals("c"))
                    aiAsClient(args[2]);
                else if (args[1].equals("s"))
                    aiAsServer(args[2]);
            }
        }
    }

    /**
     * Startet das Spiel, wenn Spieler Host ist
     *
     * @param size Spielfeldgröße
     * @throws Exception bei internem Fehler
     */
    public static void playerAsServer(String size) throws Exception {
        type = "Server";
        ai = false;
        opponent = new Op_Player(Integer.valueOf(size));
        player = new Player(Integer.valueOf(size), opponent);
        Player l = (Player) player;
        opponent.expectConnection();
    }

    /**
     * Startet das Spiel, wenn Spieler Host ist
     *
     * @param ip IP-Adresse, zu der verbunden werden soll
     * @throws LoadGameException wenn Spiel geladen werden soll
     * @throws IOException       bei Netzwerkfehlern
     */
    public static void playerAsClient(String ip) throws LoadGameException, IOException {
        type = "Client";
        ai = false;
        opponent = new Op_Player(ip);
        player = new Player(10, opponent);
        Player l = (Player) player;
        opponent.connectToServer();
        String[] newBufferEntry = opponent.getBufferedElement();
        if (newBufferEntry[0].equals("size")) {
            l.setSize(newBufferEntry[1]);
        } else if (newBufferEntry[0].equals("load")) {
            GameObject.loadGame(newBufferEntry);
            throw new LoadGameException();
        } else opponent.abortMission("size", newBufferEntry);
    }

    /**
     * Startet Spiel im Einzelspielmodus
     *
     * @param size Spielfeldgröße
     * @throws Exception bei internem Fehler
     */
    public static void aiAsServer(String size) throws Exception {
        type = "Server";
        ai = true;
        opponent = new Op_Player(Integer.valueOf(size));
        player = new Opponent_AI(Integer.valueOf(size));
        Opponent_AI l = (Opponent_AI) player;
        opponent.expectConnection();
    }

    /**
     * Hilfsmethode, mit der der Host auf den ersten Schuss wartet
     *
     * @throws IOException bei Übertragungsfehler
     */
    public static void ServerBeginningWait() throws IOException {
        String result = "";
        String[] newBufferEntry;
        do {
            newBufferEntry = opponent.getBufferedElement();//erwartet den ersten Schuss
            if (newBufferEntry[0].equals("shot")) {
                result = player.opponentShot(newBufferEntry[1], newBufferEntry[2]);
            } else opponent.abortMission("shot ROW COL", newBufferEntry);
        } while (result.equals("X") || result.equals("V"));
    }

    /**
     * Starte KI-Spiel als Client
     *
     * @param ip Adresse zu der verbunden werden soll
     * @throws Exception bei internem Fehler
     */
    public static void aiAsClient(String ip) throws Exception {
        type = "Client";
        ai = true;
        opponent = new Op_Player(ip);   //Emin: 141.18.112.222
        player = new Opponent_AI(10);
        Opponent_AI l = (Opponent_AI) player;
        opponent.connectToServer();
        String[] newBufferEntry = opponent.getBufferedElement(); //erwartet 'size'
        if (newBufferEntry[0].equals("size"))
            l.setSize(newBufferEntry[1]);
        else opponent.abortMission("size", newBufferEntry);
        opponent.sendReady();
        newBufferEntry = opponent.getBufferedElement(); //erwartet 'confirmed'
        if (newBufferEntry[0].equals("confirmed"))
            return;
        else opponent.abortMission("confirmed", newBufferEntry);
    }

    /** Starte Einzelspiel gegen die KI
     * @param _size Spielfeldgröße
     */
    public static void singlePlayer(String _size) {
        int size = Integer.valueOf(_size);
        type = "Single Player";
        ai = false;
        Opponent_AI ai = new Opponent_AI(size);
        player = new Player(size, ai);
        Player l = (Player) player;
        opponent = l;
        ai.opponent = opponent;
        ai.setShipsRandomly();
        l.opBoard = ai.myBoard;
        ai.cleanUp();
        Viewer.drawBoard(l.myBoard, l.opBoard);
    }
}
