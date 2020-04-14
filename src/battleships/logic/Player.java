package battleships.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * Klasse des Spielers.
 */
public class Player extends GameObject {
    public String aim = ""; //Speichern des letzten Schusses
    public GameObject opponent;

    public Player(int size) {
        super.size = size;
        myBoard = new String[size][size];
        opBoard = new String[size][size];
        super.shipSizes = CSVmanager.getShips("", size);
        lifes = super.calculateLifes(super.shipSizes);
        myLifes = lifes;
    }

    public Player(int size, GameObject opponent) {
        super.size = size;
        myBoard = new String[size][size];
        opBoard = new String[size][size];
        super.shipSizes = CSVmanager.getShips("", size);
        lifes = super.calculateLifes(super.shipSizes);
        myLifes = lifes;
        this.opponent = opponent;
    }

    /**
     * Das eigene Spielfeld sowie das gegnerische werden mit Schiffen gefüllt
     *
     * @param tempBoard
     */
    public void setupBoard(String[][] tempBoard) { // das  eigene Spielfeld sowie das gegnerische werden mit Schiffen gefüllt
        myBoard = tempBoard;
    }

    /**
     * Setzt die Tabellengroesse und initalisiert Attribute.
     *
     * @param _size
     */
    public void setSize(String _size) {

        super.size = Integer.valueOf(_size);
        opBoard = new String[size][size];
        myBoard = new String[size][size];
        super.shipSizes = CSVmanager.getShips("", super.size);
        lifes = 0;
        lifes = super.calculateLifes(super.shipSizes);
        myLifes = lifes;
    }



    /** Beginnt die nächste Spielfeldrunde
     * @param x Schusskoordinate
     * @param y Schusskoordinate
     * @throws IOException bei Nezwerkfehler
     * @throws GameOverException
     */
    public void startGame(int x, int y) throws IOException, GameOverException {
        if (lifes > 0 && myLifes > 0) //Wenn ich und mein Gegner noch am Leben
        {
            if (opponent instanceof Op_Player) {
                opponent.sendShot(x + "", y + "");
                String[] newBufferEntry = opponent.getBufferedElement();//erwartet 'answer'
                if (newBufferEntry[0].equals("answer")) {
                    aim = newBufferEntry[1];
                } else abortMission("answer INT", newBufferEntry);
            } else {
                if (opBoard[x][y] != null && opBoard[x][y] != "w") {
                    opponent.myBoard[x][y] = "X";
                    if (opponent.sank(x, y))
                        aim = "2";
                    else aim = "1";

                } else aim = "0";
            }
            if (aim != null && !aim.equals("0")) { //Wenn kein Wasser
                if (aim.charAt(0) == '1') { //überflüssig?
                    opBoard[x][y] = "X";
                    lifes--;
                }
                if (aim.equals("2")) {
                    opBoard[x][y] = "X";
                    surroundWithWater(opBoard, x, y);
                    lifes--;
                }

            } else {
                opBoard[x][y] = "w";
            }
        }
        if (lifes < 1) { //Wenn der Gegner keine Leben mehr hat
            System.err.println("Du (Spieler) hast deinen Gegner ZERSTÖRT!");
            throw new GameOverException(true);
        }
        if (myLifes < 1) { //Wenn ich keine Leben mehr habe
            System.err.println("Du (Spieler) wurdest vom Gegner ZERSTÖRT!");
            throw new GameOverException(false);
        }
    }

    /** Entscheidet, wer als nächstes schießen darf
     * @param aim letzter Schuss
     * @throws IOException bei Netzwerkfehler
     * @throws GameOverException wenn Spiel vorbei
     */
    public void nextTurn(String aim) throws IOException, GameOverException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = "";

        if (aim == null || (aim.equals("0"))) {
            if (result.equals("w") || result.equals(""))
                opponent.sendPass();
            while (lifes > 0 && myLifes > 0 && result != null && (result.equals("X") || result.equals("V") || result.equals(""))) {
                String[] newBufferEntry;
                if ((opponent instanceof Op_Player)) {
                    newBufferEntry = opponent.getBufferedElement();
                    if (newBufferEntry[0].equals("shot")) {
                        result = opponentShot(newBufferEntry[1], newBufferEntry[2]);
                        Viewer.drawBoard(myBoard, opBoard);
                    } else abortMission("shot ROW COL", newBufferEntry);
                } else result = opponent.fire();
            }

        }
        if (lifes < 1) { //Wenn der Gegner keine Leben mehr hat
            System.err.println("Du (Spieler) hast deinen Gegner ZERSTÖRT!");
            throw new GameOverException(true);
        }
        if (myLifes < 1) { //Wenn ich keine Leben mehr habe
            System.err.println("Du (Spieler) wurdest vom Gegner ZERSTÖRT!");
            throw new GameOverException(false);
        }
        Viewer.drawBoard(myBoard, opBoard);

    }

    /**
     * @param x Schusskoordinate
     * @param y Schusskoordinate
     * @throws IOException bei Netzwerkfehler
     */
    public void sendShot(String x, String y) throws IOException {
        if (opponent instanceof Op_Player)
            this.opponentShot(x, y);
    }

    /**Verwertet Schuss intern
     * @param xAsString x-Schusskoordinate
     * @param yAsString y-Schusskoordinate
     * @return
     * @throws IOException bei Netzwerkfehler
     */
    public String opponentShot(String xAsString, String yAsString) throws IOException {
        int x = Integer.valueOf(xAsString);
        int y = Integer.valueOf(yAsString);
        if (myBoard[x][y] != null && (myBoard[x][y].charAt(0) == 'S' || myBoard[x][y].equals("X"))) {
            myBoard[x][y] = "X";
            myLifes--; //Bei einem Treffer gehen die eigenen Leben runter

            if (sank(x, y)) {
                Viewer.drawBoard(myBoard, opBoard);
                Viewer.drawBoard(myBoard, opBoard);
                opponent.sendAnswer(2);
                return "V";
            } else opponent.sendAnswer(1);
        } else {
            myBoard[x][y] = "w";
            opponent.sendAnswer(0);
            String[] newBufferEntry = opponent.getBufferedElement();//erwartet 'answer'
            if (opponent instanceof Op_Player && !newBufferEntry[0].equals("pass")) {
                abortMission("pass", newBufferEntry);
            }
        }

        return myBoard[x][y]; //  Gibt Wasser oder Schiff zurück
    }


}
