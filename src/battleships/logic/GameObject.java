package battleships.logic;

import battleships.GUI.SuperController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public class GameObject {
    public int lifes;
    public int myLifes;
    public String[][] myBoard;
    public String[][] opBoard;
    public static int size;
    public static int[] shipSizes;
    public int[] shipOrder = {5, 4, 3, 2};

    /**
     * Hilfsmethode die prüft, ob übergebenes Feld gültig ist.
     *
     * @param newBoard Spielfeld mit neu hinzugefügten Schiffen
     * @return true, wenn Spielfeld gültig ist
     */
    public boolean setShips(String[][] newBoard) {
        boolean newShipFound = false;
        if (newBoard.equals(myBoard)) {
            return false;
        } //Wenn Spielbretter identisch, bedeutet das, dass das neue Schiff auf einem anderen liegt
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (newBoard[i][j] != null && newBoard[i][j].charAt(0) == 'S' && (myBoard[i][j] == null || myBoard[i][j] != null && myBoard[i][j].charAt(0) != 'S')) { //wenn neues Schiff erkannt
                    newShipFound = true;
                    if (myBoard[i][j] != null && (myBoard[i][j].equals("w") || myBoard[i][j].equals("S"))) {
                        return false;
                    }  //Wenn erstes Feld Wasser
                    if (j + 1 < size && newBoard[i][j + 1] != null && !newBoard[i][j + 1].equals(myBoard[i][j + 1])) { //Wenn Schiff quer liegt
                        if (j - 1 > -1) {
                            newBoard[i][j - 1] = "w"; //Wasser an den Kopf setzen
                            if (i - 1 > -1)
                                newBoard[i - 1][j - 1] = "w"; //Wasser oben neben Kopf setzen
                            if (i + 1 < myBoard.length)
                                newBoard[i + 1][j - 1] = "w"; //Wasser unten neben Kopf setzen
                        }
                        j--;
                        while (j + 1 < size && newBoard[i][j + 1] != null
                                && !newBoard[i][j + 1].equals(myBoard[i][j + 1])) { //über das Schiff iterierten
                            if (myBoard[i][j + 1] != null && (myBoard[i][j + 1].equals("w") || myBoard[i][j + 1].equals("S"))) {
                                return false;
                            }
                            if (i + 1 < size && (newBoard[i + 1][j + 1] == null)) {
                                newBoard[i + 1][j + 1] = "w";
                            } //Wasser drunter
                            if (i - 1 > -1 && newBoard[i - 1][j + 1] == null) {
                                newBoard[i - 1][j + 1] = "w";
                            }  //Wasser drüber
                            j++;
                        }
                        if (j + 1 < size && newBoard[i][j + 1] == null) {
                            newBoard[i][j + 1] = "w"; //Wasser an den Fuß setzen
                            if (i - 1 > -1)
                                newBoard[i - 1][j + 1] = "w"; //Wasser oben neben Kopf setzen
                            if (i + 1 < myBoard.length)
                                newBoard[i + 1][j + 1] = "w"; //Wasser unten neben Kopf setzen

                        }

                    } else { //Wenn Schiff längs liegt
                        if (i - 1 > -1) {
                            newBoard[i - 1][j] = "w";  //Wasser an den Kopf setzen
                            if (j - 1 > -1)
                                newBoard[i - 1][j - 1] = "w";  //Wasser oben an den Kopf setzen
                            if (j + 1 < myBoard.length)
                                newBoard[i - 1][j + 1] = "w";  //Wasser unten an den Kopf setzen
                        }
                        i--;
                        while (i + 1 < size && newBoard[i + 1][j] != null
                                && !newBoard[i + 1][j].equals(myBoard[i + 1][j])) { //über das Schiff iterierten
                            if (myBoard[i + 1][j] != null && (myBoard[i + 1][j].equals("w") || myBoard[i + 1][j].equals("S"))) {
                                return false;
                            }
                            if (j + 1 < size && (newBoard[i + 1][j + 1] == null)) {
                                newBoard[i + 1][j + 1] = "w";
                            } //Wasser drunter
                            if (j - 1 > -1 && newBoard[i + 1][j - 1] == null) {
                                newBoard[i + 1][j - 1] = "w";
                            }  //Wasser drüber
                            i++;
                        }
                        if (i + 1 < size && newBoard[i + 1][j] == null) {
                            newBoard[i + 1][j] = "w";  //Wasser an den Fuß setzen
                            if (j - 1 > -1)
                                newBoard[i + 1][j - 1] = "w";  //Wasser oben an den Fuß setzen
                            if (j + 1 < myBoard.length)
                                newBoard[i + 1][j + 1] = "w";  //Wasser oben an den Fuß setzen
                        }
                    }
                }
            }
        }
        if (!newShipFound) {
            return false;
        } //Wenn kein neues Schiff gefunden, liegt es auf bzw. innerhalb eines anderen Schiffes, was ungültig ist

        for (int i = 0; i < myBoard.length; i++) {
            for (int j = 0; j < myBoard.length; j++) {
                myBoard[i][j] = newBoard[i][j];
            }
        }

        return true;
    }

    /**
     * Setzt die Schiffe auf dem Spielfeld zufaellig.
     */
    public void setShipsRandomly() {
        int counter = 0;
        String[][] temp = new String[size][size];
        int x, y;
        int[] shipSizes = CSVmanager.getShips("", size);
        Random r = new Random();
        for (int k = 0; k < shipSizes.length; k++) {
            for (int l = 0; l < shipSizes[k]; l++) {

                int result = r.nextInt(2);
                if (result == 1) {  //Für jedes Schiff zuerst seine Ausrichtung bestimmen
                    y = r.nextInt(size); //y ist fest;
                    x = r.nextInt(size - shipOrder[k]);  //Stellt sicher, dass das Schiffsende nicht außerhalb des Brettes liegt

                    for (int i = 0; i < myBoard.length; i++) {
                        for (int j = 0; j < myBoard.length; j++) {
                            temp[i][j] = myBoard[i][j];
                        }
                    }  //Eventuell nicht gesetzte Schiffe löschen
                    for (int i = 0; i < shipOrder[k]; i++) {  //Einfügen des neuen Schiffes
                        temp[x + i][y] = "S";
                    }
                    if (!setShips(temp)) {
                        l--;
                        continue;
                    } //nochmals für dieses Schiff eine Position suchen

                } else {
                    x = r.nextInt(size); //x ist fest;
                    y = r.nextInt(size - shipOrder[k]); //Stellt sicher, dass das Schiffsende nicht außerhalb des Brettes liegt
                    for (int i = 0; i < myBoard.length; i++) {
                        for (int j = 0; j < myBoard.length; j++) {
                            temp[i][j] = myBoard[i][j];
                        }
                    }  //Eventuell nicht gesetzte Schiffe löschen
                    for (int i = 0; i < shipOrder[k]; i++) { //Einfügen des neuen Schiffes
                        temp[x][y + i] = "S";
                    }
                    if (!setShips(temp)) {
                        ;
                        l--;
                        continue;
                    } //nochmals für dieses Schiff eine Position suchen
                }
            }
        }
    }

    /**
     * Hilfsmethode die ermittelt, ob Schiff versenkt wurden.
     *
     * @param ox Eine beliebige x-Koordninate des Schiffes
     * @param oy Eine beliebige y-Koordninate des Schiffes
     * @return true, wenn Schiff versenkt
     */
    public boolean sank(int ox, int oy) {
        int x = ox;
        while (x > -1 && myBoard[x][oy] != null && !myBoard[x][oy].equals("w")) {
            if (myBoard[x][oy].equals("S")) {
                return false;
            }
            x--;
        }
        x = ox;
        while (x < size && myBoard[x][oy] != null && !myBoard[x][oy].equals("w")) {
            if (myBoard[x][oy].equals("S")) {
                return false;
            }
            x++;
        }
        int y = oy;
        while (y > -1 && myBoard[ox][y] != null && !myBoard[ox][y].equals("w")) {
            if (myBoard[ox][y].equals("S")) {
                return false;
            }
            y--;
        }
        y = oy;
        while (y < size && myBoard[ox][y] != null && !myBoard[ox][y].equals("w")) {
            if (myBoard[ox][y].equals("S")) {
                return false;
            }
            y++;
        }


        return true;
    }

    /**
     * Hilfsmethode um Wasser zu entfernen.
     *
     * @param myBoard gewünschtes Spielfeld mit Wasser
     * @return das Spielfeld ohne Wasser
     */
    public String[][] cleanUp(String[][] myBoard) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (myBoard[i][j] != null && myBoard[i][j].equals("w"))
                    myBoard[i][j] = null;
            }
        }
        return myBoard;
    }

    public String[][] cleanUp() {
        return cleanUp(this.myBoard);
    }

    public int counter = 0;

    /**
     * Hilfsmethode um Schiff mit Wasser zu umzingeln.
     *
     * @param board Gewünschtes Spielfeld
     * @param x     Eine beliebige x-Koordninate des Schiffes
     * @param y     Eine beliebige y-Koordninate des Schiffes
     */
    public void surroundWithWater(String[][] board, int x, int y) {
        ArrayList<Position> pos = new ArrayList<>();
        int lauf = 0;  //Wird in den if-Abfragen als Laufvariable genutzt

        if (x - 1 >= 0 && (board[x - 1][y] != null && (board[x - 1][y].equals("X") || board[x - 1][y].equals("S") || board[x - 1][y].equals("V")))) {
            //SCHIFF GEHT NACH OBEN WEITER
            lauf = x;

            while (lauf >= 0 && (board[lauf][y] != null && (board[lauf][y].equals("X") || board[lauf][y].equals("S") || board[lauf][y].equals("V")))) {
                pos.add(new Position(lauf, y)); //Fuege Position zur Liste hinzu
                lauf--;
            }
        }

        if (x + 1 < board.length && (board[x + 1][y] != null && (board[x + 1][y].equals("X") || board[x + 1][y].equals("S") || board[x + 1][y].equals("V")))) {
            //SCHIFF GEHT NACH UNTEN WEITER
            lauf = x;

            while (lauf < board.length && (board[lauf][y] != null && (board[lauf][y].equals("X") || board[lauf][y].equals("S") || board[lauf][y].equals("V")))) {
                pos.add(new Position(lauf, y)); //Fuege Position zur Liste hinzu
                lauf++;
            }
        }

        if (y - 1 >= 0 && (board[x][y - 1] != null && (board[x][y - 1].equals("X") || board[x][y - 1].equals("S") || board[x][y - 1].equals("V")))) {
            //SCHIFF GEHT NACH LINKS WIETER
            lauf = y;

            while (lauf >= 0 && (board[x][lauf] != null && (board[x][lauf].equals("X") || board[x][lauf].equals("S") || board[x][lauf].equals("V")))) {
                pos.add(new Position(x, lauf)); //Fuege Position zur Liste hinzu
                lauf--;
            }
        }

        if (y + 1 < board.length && (board[x][y + 1] != null && (board[x][y + 1].equals("X") || board[x][y + 1].equals("S") || board[x][y + 1].equals("V")))) {
            //SCHIFF GEHT NACH RECHTS WIETER
            lauf = y;

            while (lauf < board.length && (board[x][lauf] != null && (board[x][lauf].equals("X") || board[x][lauf].equals("S") || board[x][lauf].equals("V")))) {
                pos.add(new Position(x, lauf)); //Fuege Position zur Liste hinzu
                lauf++;
            }
        }

        // Jetzt stehen alle Positionen des Schiffs in der ArrayList 'pos'
        // Iteriere ueber 'pos' und umzingel alle Positionen mit Wasser, falls moeglich
        for (Position schiffspos : pos) {

            if (schiffspos.x - 1 >= 0 && board[schiffspos.x - 1][schiffspos.y] == null) {
                //ZEICHNE OBERHALB DES FELDES WASSER
                board[schiffspos.x - 1][schiffspos.y] = "w";
            }
            if (schiffspos.x + 1 < board.length && board[schiffspos.x + 1][schiffspos.y] == null) {
                //ZEICHNE UNTERHALB DES FELDES WASSER
                board[schiffspos.x + 1][schiffspos.y] = "w";
            }
            if (schiffspos.y - 1 >= 0 && board[schiffspos.x][schiffspos.y - 1] == null) {
                //ZEICHNE LINKS DES FELDES WASSER
                board[schiffspos.x][schiffspos.y - 1] = "w";
            }
            if (schiffspos.y + 1 < board.length && board[schiffspos.x][schiffspos.y + 1] == null) {
                //ZEICHNE RECHTS DES FELDES WASSER
                board[schiffspos.x][schiffspos.y + 1] = "w";
            }
            if (schiffspos.x - 1 >= 0 && schiffspos.y - 1 >= 0 && board[schiffspos.x - 1][schiffspos.y - 1] == null) {
                //ZEICHNE LINKS OBERHALB DES FELDES WASSER
                board[schiffspos.x - 1][schiffspos.y - 1] = "w";
            }
            if (schiffspos.x - 1 >= 0 && schiffspos.y + 1 < board.length && board[schiffspos.x - 1][schiffspos.y + 1] == null) {
                //ZEICHNE RECHTS OBERHALB DES FELDES WASSER
                board[schiffspos.x - 1][schiffspos.y + 1] = "w";
            }
            if (schiffspos.x + 1 < board.length && schiffspos.y - 1 >= 0 && board[schiffspos.x + 1][schiffspos.y - 1] == null) {
                //ZEICHNE LINKS UNTERHALB DES FELDES WASSER
                board[schiffspos.x + 1][schiffspos.y - 1] = "w";
            }
            if (schiffspos.x + 1 < board.length && schiffspos.y + 1 < board.length && board[schiffspos.x + 1][schiffspos.y + 1] == null) {
                //ZEICHNE RECHTS UNTERHALB DES FELDES WASSER
                board[schiffspos.x + 1][schiffspos.y + 1] = "w";
            }

        }

    }

    /**
     * Klasse zum speichern für die X und Y Koordinaten.
     */
    private class Position {

        int x = 0;
        int y = 0;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return x + ", " + y;
        }
    }

    /**
     * Laed ein gespeichertes Spiel vom Gegner
     *
     * @param recieved Nachricht vom Gegner.
     */
    public static void loadGame(String[] recieved) {
        if (recieved[0].equals("load")) {
            if (CSVmanager.load(recieved[1] + ".CSV"))
                Launcher.type = "Server";
            else Launcher.type = "Client";

        }
        ;
    }

    /**
     * Gibt Fehlermeldung aus und beendet, falls eine unerwartete oder Falsche Nachricht empfangen wurde
     *
     * @param expected Eigentlich erwatete Nachricht
     * @param recieved Tatsächlich empfangene Nachricht
     */
    public void abortMission(String expected, String[] recieved) {
        if (recieved[0].equals("save")) {
            System.err.println("Gegener hat gespeichert und beendet");
            CSVmanager.saveWithID(Launcher.player.myBoard, Launcher.player.opBoard, recieved[1], false, myLifes, lifes);
            System.exit(1);
        }
        System.err.println("-----------UEBERTRAGUNSFEHLER!---------");
        System.err.println("Statt der erwarteten Nachricht '" + expected + "' wurde '" + recieved[0] + "' empfangen!");
        System.exit(0);
    }

    /**
     * Berechnet die Leben für übergebene Schiffe.
     *
     * @param ships Die Schiffsbelegung bei der aktuellen Spielfeldgröße
     * @return Anzahl der Leben
     */
    public int calculateLifes(int[] ships) { //Mehtode berechnent die Leben des Spielers äbhängig von den verwendeten Schiffen
        lifes = 0;
        for (int i = 0; i < 4; i++) {
            lifes += (5 - i) * ships[i];  //1. Schiff zählt fünffach, 2. Schiff vierfach usw...
        }
        return lifes;
    }

    public void sendShot(String x, String y) throws IOException {
    }

    public void sendSave(long id) throws IOException {
    }

    ;

    public void sendLoad(long id) throws IOException {
    }

    ;

    public void sendPass() throws IOException {
    }

    public void setSize(String size) {
    }

    public void sendAnswer(int value) throws IOException {
    }

    public void sendReady() throws IOException {
    }

    public void expectInput() throws IOException {
        return;
    }

    public void opponentReady() throws IOException {
    }

    public String opponentShot(String x, String y) throws IOException {
        return null;
    }

    public String[] getBufferedElement() {
        return null;
    }

    public String fire() throws IOException, GameOverException {
        return null;
    }

    public void expectConnection() throws IOException {
    }

    public void sendFieldsize() {
    }

    public void connectToServer() throws IOException {
    }


}
