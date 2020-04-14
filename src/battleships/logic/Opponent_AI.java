package battleships.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Klasse der Kuenstlichen Intelligenz.
 */
public class Opponent_AI extends GameObject {
    public int x = 2, y = 5, firstX, firstY, i = -1, j = -1;
    public int counter;
    public String result; // Inhalt des Feldes auf das geschossen wurde (w oder X)
    public String rotation = "x"; // Ausrichtung eines lokalisierten Schiffes, x für senkrecht, y für längs
    public boolean uncovered = false; // Ein Schiff gilt als uncovered, wenn es einmal getroffen wurde
    public boolean localized = false; // Ein Schiff gilt als localized, wenn es zwei mal getroffen wurde und dessen Ausrichtung (längs oder quer) somit feststeht
    public boolean even = true;
    public boolean sank = false;
    public GameObject opponent; //der Gegner ist entweder der Player, oder eine andere KI

    Opponent_AI(int size) {
        super.size = size;
        opBoard = new String[size][size];
        myBoard = new String[size][size];
        super.shipSizes = CSVmanager.getShips("", size);
        lifes = super.calculateLifes(super.shipSizes);
        myLifes = lifes;
        opponent = Launcher.opponent;
    }

    /**
     * Setzt die Tabellengroesse und initalisiert Attribute.
     *
     * @param _size Tabllengroesse.
     */
    public void setSize(String _size) {

        super.size = Integer.valueOf(_size);
        opBoard = new String[size][size];
        myBoard = new String[size][size];
        shipSizes = CSVmanager.getShips("", size);
        lifes = 0;
        lifes = super.calculateLifes(shipSizes);
        myLifes = lifes;
        setShipsRandomly();
        cleanUp();
        Viewer.drawBoard(myBoard, myBoard);
        opponent = Launcher.opponent;
    }

    /**
     * Methode, mit der die KI schießt
     * @return
     * @throws IOException bei Netzwerkfehler
     * @throws GameOverException
     */
    public String fire() throws IOException, GameOverException {
        if (lifes > 0) {
            if (!uncovered) { // Phase1: solange noch kein Schiff "enttarnt" wurde, zufällig schiessen
                fireRandomly();

            } else if (uncovered && !localized) { //Phase2: Wenn Schiff enttarnt, muss nun dessen Ausrichtung (längs oder quer) durch Beschuss der Umlegenden Felder festgestellt werden
                localizeShip();
            } else if (uncovered && localized) { //Phase3: Wenn Schiff lokalisiert, kann es versenkt werden
                sinkShip();
            }
            if (opponent instanceof Op_Player) {
                nextTurn(result);
            } else return result;
        }
        if (lifes < 1) { //Wenn KI Gegner keine Leben mehr hat
            if (Launcher.ai)
                throw new GameOverException(true);
            return "KI hat gewonnen";
        }
        if (myLifes < 1) { //Wenn ich keine Leben mehr habe
            if (Launcher.ai)
                throw new GameOverException(false);
            return "KI hat verloren";
        }
        return "";
    }

    /** 1. KI-Phase: Zufälliges Schießen
     * @throws IOException bei Netzwerkfehler
     */
    private void fireRandomly() throws IOException {
        Random random = new Random();
        String[] newEntry;
        while (!uncovered) {
            x = random.nextInt(size);  //Zufälliges wählen der Zeile im Bereich 0...size -1
            y = random.nextInt(size);   //Zufälliges wählen der Spalte im Bereich 0...size -1
            if (opBoard[x][y] != null) continue; // Wenn an diese Position schon geschossen wurde, nochmal feuern
            opponent.sendShot(x + "", y + "");  //Koordnitaten an Spieler "schicken"
            if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                newEntry = opponent.getBufferedElement();
                if (newEntry[0].equals("answer")) {
                    result = newEntry[1];
                } else abortMission("answer INT", newEntry);
            } else result = opponent.opponentShot(x + "", y + "");

            if (answerToSymbol(result) != null && answerToSymbol(result).equals("X")) { // Wenn ein Schiff des Spielers getroffen wurde
                uncovered = true;
                opBoard[x][y] = "X";
                lifes--;
                return;
            } else {
                opBoard[x][y] = "w";
                return;
            }
        }
    }

    /** 2. KI-Phase: Schiff um Treffer lokalisieren
     * @throws IOException bei Netzwerkfehler
     */
    private void localizeShip() throws IOException {
        Viewer.disabled = false;
        Viewer.drawBoard(myBoard, opBoard);
        Viewer.disabled = true;
        String[] newEntry;
        String symbol = "";
        if (x + 1 < size && (opBoard[x + 1][y] == null)) { //Wenn Feld drunter noch frei
            opponent.sendShot("" + (x + 1), "" + y);
            if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                newEntry = opponent.getBufferedElement();
                if (newEntry[0].equals("answer")) {
                    result = newEntry[1];
                } else abortMission("answer INT", newEntry);
            } else result = opponent.opponentShot((x + 1) + "", y + "");
            opBoard[x + 1][y] = answerToSymbol(result);
            if (answerToSymbol(result) != null && answerToSymbol(result).equals("X")) { // Wenn ein Schiff des Spielers getroffen wurde
                localized = true;
                lifes--;
                firstX = x;
                x++;
                rotation = "x"; //Schiff liegt quer im Spielfeld

            }
            if (answerToSymbol(result).equals("V")) {
                surroundWithWater(opBoard, x + 1, y);
                lifes--;
                uncovered = false;
            }
            return;
        }

        if (x - 1 > -1 && (opBoard[x - 1][y] == null)) { //Wenn Feld drüber noch frei
            opponent.sendShot("" + (x - 1), "" + y);
            if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                newEntry = opponent.getBufferedElement();
                if (newEntry[0].equals("answer")) {
                    result = newEntry[1];
                } else abortMission("answer INT", newEntry);
            } else result = opponent.opponentShot((x - 1) + "", y + "");
            opBoard[x - 1][y] = answerToSymbol(result);
            if (answerToSymbol(result) != null && answerToSymbol(result).equals("X")) { // Wenn ein Schiff des Spielers getroffen wurde
                localized = true;
                lifes--;
                firstX = x;
                x--;
                rotation = "x"; //Schiff liegt quer im Spielfeld

            }
            if (answerToSymbol(result).equals("V")) {
                surroundWithWater(opBoard, x - 1, y);
                uncovered = false;
                lifes--;
            }
            return;
        }
        if (y + 1 < size && (opBoard[x][y + 1] == null)) { //Wenn Feld rechts noch frei
            opponent.sendShot("" + x, "" + (y + 1));
            if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                newEntry = opponent.getBufferedElement();
                if (newEntry[0].equals("answer")) {
                    result = newEntry[1];
                } else abortMission("answer INT", newEntry);
            } else result = opponent.opponentShot(x + "", (y + 1) + "");
            opBoard[x][y + 1] = answerToSymbol(result);
            if (answerToSymbol(result) != null && answerToSymbol(result).equals("X")) { // Wenn ein Schiff des Spielers getroffen wurde
                localized = true;
                lifes--;
                firstY = y;
                y++;
                rotation = "y"; //Schiff liegt längs im Spielfeld
            }
            if (answerToSymbol(result).equals("V")) {
                surroundWithWater(opBoard, x, y + 1);
                uncovered = false;
                lifes--;
            }
            return;
        }
        if (y - 1 > -1 && (opBoard[x][y - 1] == null)) { //Wenn Feld links noch frei
            opponent.sendShot("" + x, "" + (y - 1));
            if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                newEntry = opponent.getBufferedElement();
                if (newEntry[0].equals("answer")) {
                    result = newEntry[1];
                } else abortMission("answer INT", newEntry);
            } else result = opponent.opponentShot(x + "", (y - 1) + "");
            opBoard[x][y - 1] = answerToSymbol(result);
            if (answerToSymbol(result) != null && answerToSymbol(result).equals("X")) { // Wenn ein Schiff des Spielers getroffen wurde
                localized = true;
                lifes--;
                firstY = y;
                y--;
                rotation = "y"; //Schiff liegt längs im Spielfeld
            }
            if (answerToSymbol(result).equals("V")) {
                surroundWithWater(opBoard, x, y - 1);
                uncovered = false;
                lifes--;
            }
            return;
        }
    }

    /** 3. KI-Phase: Versenken des gefundenen Schiffes
     * @throws IOException bei Netzwerk
     */
    private void sinkShip() throws IOException {
        if (rotation.equals("x")) {//Wenn das Schiff quer liegt
            startSinking(x, firstX);
            return;
        }
        if (rotation.equals("y")) { // Wenn das Schiff läängs liegt
            startSinking(y, firstY);
            return;
        }
    }

    /** Hilfsmethode zum eigentlichen Versenken
     * @param shot1 erster Treffer der KI
     * @param shot2 zweiter Treffer der KI
     * @throws IOException bei Netwerkfehler
     */
    private void startSinking(int shot1, int shot2) throws IOException {
        String[] newBufferEntry;
        while (lifes > 0) {

            if (i == -1) { //Anfängliche Initalisierung der Variablen
                i = Math.max(shot1, shot2);
                j = Math.min(shot1, shot2);
                i++;
                j--;
            } // Mit i-Zeiger nach oben zählen, mit j-Zeiger nach unten

            if (even) {// es soll abwechselnd geschossen werden
                counter = i;
            } else {
                counter = j;
            }
            if (counter < size && counter > -1) {  //innerhalb des Spielfelds
                if (rotation.equals("x")) {  //x-Wert wird verändert, y Wert bleibt
                    x = counter;
                } else { // y-Wert wird verändert, x-Wert bleibt gleich
                    y = counter;
                }
                if (opBoard[x][y] == null || !opBoard[x][y].equals("X")) { //Nur schießen, wenn Feld noch nie beschossen wurde
                    opponent.sendShot("" + x, "" + y);
                    if (opponent instanceof Op_Player) {  //Wenn Spiel gegen KI
                        newBufferEntry = opponent.getBufferedElement();
                        if (newBufferEntry[0].equals("answer")) {
                            result = newBufferEntry[1];
                        } else abortMission("answer INT", newBufferEntry);
                    } else result = opponent.opponentShot(x + "", y + "");
                } else {
                    Viewer.disabled = false;
                    Viewer.drawBoard(myBoard, opBoard);
                    Viewer.disabled = true;
                    even = false; //ansonsten neues Feld suchen
                    continue;
                }
                opBoard[x][y] = answerToSymbol(result);
                if (answerToSymbol(result).equals("X") || answerToSymbol(result).equals("V")) {//Solange bis Wasser getroffen
                    lifes--;
                    if (answerToSymbol(result).equals("V")) { //Wenn Schiff versenkt
                        surroundWithWater(opBoard, x, y);
                        i = -1;
                        j = -1;
                        sank = false;
                        uncovered = false;
                        even = true;
                        localized = false;
                        Viewer.drawBoard(myBoard, opBoard);
                        return;

                    }
                } else {//Wenn Wasser getroffen
                    even = false;
                }
                if (counter == i) {
                    i++;
                } else {
                    assert counter == j : "Falsch gezählt!";
                    j--;
                }
                return;

            } else { // am Rand des Spielfeldes
                Viewer.disabled = false;
                Viewer.drawBoard(opBoard, opBoard);
                Viewer.disabled = true;
                even = false;
                sank = true;
                if (lifes > 0 && !sank(x, y))
                    continue;
            }


        }
    }

    /** Verwertet den gegnerischen Schuss
     * @param xAsString x - Koordinate des Schusses
     * @param yAsString y - Koordinate des Schusses
     * @return internes Symbol, das das Resultat repraesentiert
     * @throws IOException bei Netzwerkfehler
     */
    public String opponentShot(String xAsString, String yAsString) throws IOException {
        String[] newBufferEntry;
        int x = Integer.valueOf(xAsString);
        int y = Integer.valueOf(yAsString);
        if (myBoard[x][y] != null && (myBoard[x][y].charAt(0) == 'S' || myBoard[x][y].equals("X"))) {
            myBoard[x][y] = "X";
            myLifes--;
            if (sank(x, y)) {
                opponent.sendAnswer(2);
                return "V";
            } else opponent.sendAnswer(1);
        } else {
            myBoard[x][y] = "w";
            opponent.sendAnswer(0);
            newBufferEntry = opponent.getBufferedElement();
            if (!newBufferEntry[0].equals("pass")) {
                abortMission("pass", newBufferEntry);
            }
        }
        return myBoard[x][y]; //  Gibt Wasser oder Schiff zurück
    }

    /** Bestimmt, wer als nächstes dran ist
     * @param aim Resultat des letzten Schusses
     * @throws IOException
     */
    public void nextTurn(String aim) throws IOException {  //Bei 0: Pass schicken, shot erwarten. Ansonsten: Nochmal schießen
        String[] newBufferEntry;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String result = "";

        try {
            Thread.sleep(0);
        } catch (Exception e) {
        }
        if (aim == null || (aim.equals("0"))) {
            if (result.equals("w") || result.equals(""))
                opponent.sendPass();
            while (lifes > 0 && myLifes > 0 && result != null && (result.equals("X") || result.equals("V") || result.equals(""))) {
                newBufferEntry = opponent.getBufferedElement();
                if (newBufferEntry[0].equals("shot")) {
                    result = opponentShot(newBufferEntry[1], newBufferEntry[2]);
                    Viewer.drawBoard(myBoard, opBoard);
                } else abortMission("shot ROW COL", newBufferEntry);
            }
        }
        //Wenn der Gegner ein Schiff getroffen hat, darf er nochmal
        Viewer.drawBoard(myBoard, opBoard);

    }

    /** Interne Hilfsmethode zum Umwandeln der Schiffe in Symbol
     * @param result
     * @return
     */
    private String answerToSymbol(String result) {
        switch (result) {
            case "0":
                return "w";
            case "1":
                return "X";
            case "2":
                return "V";
            default:
                return result;
        }
    }

}

