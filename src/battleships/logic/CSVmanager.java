package battleships.logic;

import battleships.GUI.SuperController;

import javax.swing.text.View;
import java.io.*;
import java.util.Arrays;

/**
 *
 */
public class CSVmanager {

    static String shipPath = new File("Schiffeversenken2.0/src/schiffstabelle.CSV").getAbsolutePath();

    /**
     * @param file,     Der Speicherort der Schiffstabelle
     * @param boardSize Die gewünschte Feldgröße
     *                  Berechnet bei gegebener Feldgröße ein int Array mit der Schiffsanzahl für den jeweiligen Schiffstyp [5er, 4er, 3er, 2er]
     * @return ein int Array mit der Schiffsanzahl für den jeweiligen Schiffstyp [5er, 4er, 3er, 2er]
     */
    public static int[] getShips(String file, int boardSize) {
        String line = "";
        file = shipPath;
        String[] shipsAsStrings;
        int[] ships = new int[0];
        int counter = 4;
        try (
                BufferedReader reader = new BufferedReader(new FileReader(file))) {

            while ((line = reader.readLine()) != null) {
                if (counter == boardSize) {
                    shipsAsStrings = line.split(",");
                    ships = new int[shipsAsStrings.length - 1];
                    for (int i = 0; i < shipsAsStrings.length - 1; i++) {
                        ships[i] = Integer.valueOf(shipsAsStrings[i]);
                    }
                    break;
                } else {
                    counter++;
                }


            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return ships;
    }

    /**
     * @param board0  eigenes Spielfeld
     * @param board1  Spielfeld mit eigenen Schüssen
     * @param name    ID, mit der die Datei gespeichert wird
     * @param myTurn  Boolean der angibt ob ich oder Gegner gespeichert hat
     * @param myLifes Eigene Leben
     * @param lifes   Gegnerische Leben
     *                Speichert aktuellen Spielstand bei gegebener ID
     * @return false im Fehlerfall
     */
    public static boolean saveWithID(String[][] board0, String[][] board1, String name, boolean myTurn, int myLifes, int lifes) {
        int size = board0.length;
        try (
                Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(name + ".CSV"), "utf-8"))) {
            writer.write("" + size + "," + myTurn + "," + myLifes + "," + lifes);
            ((BufferedWriter) writer).newLine();
            for (int i = 0; i < board0.length; i++) {
                for (int j = 0; j < board0.length; j++) {
                    if (board0[i][j] != null) {
                        writer.write(board0[i][j] + ",");
                    } else writer.write(".,");
                }
                ((BufferedWriter) writer).newLine();
            }
            ((BufferedWriter) writer).newLine();

            for (int i = 0; i < board1.length; i++) {
                for (int j = 0; j < board1.length; j++) {
                    if (board1[i][j] != null) {
                        writer.write(board1[i][j] + ",");
                    } else writer.write(".,");
                }
                ((BufferedWriter) writer).newLine();
            }

            //Spielbrett der KI
            if (Launcher.type.equals("Single Player")) {
                Opponent_AI ki = ((Opponent_AI) ((Player) Launcher.player).opponent);
                writer.write("SINGLE PLAYER");
                ((BufferedWriter) writer).newLine();
                for (int i = 0; i < ki.myBoard.length; i++) {
                    for (int j = 0; j < ki.myBoard.length; j++) {
                        if (ki.myBoard[i][j] != null) {
                            writer.write(ki.myBoard[i][j] + ",");
                        } else writer.write(".,");
                    }
                    ((BufferedWriter) writer).newLine();
                }
                writer.write("SINGLE PLAYER2");
                ((BufferedWriter) writer).newLine();
                //Gegnerisches Brett der KI
                for (int i = 0; i < ki.opBoard.length; i++) {
                    for (int j = 0; j < ki.opBoard.length; j++) {
                        if (ki.opBoard[i][j] != null) {
                            writer.write(ki.opBoard[i][j] + ",");
                        } else writer.write(".,");
                    }
                    ((BufferedWriter) writer).newLine();
                }
                ((BufferedWriter) writer).newLine();

                //Abspeichern zusätzlicher KI-Zustände
                writer.write(ki.x + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.y + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.firstX + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.firstY + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.i + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.j + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.counter + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.result + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.rotation + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.uncovered + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.localized + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.sank + "");
                ((BufferedWriter) writer).newLine();
                writer.write(ki.even + "");
                ((BufferedWriter) writer).newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Schreiben der Datei fehlgeschlagen.");
        }

        return true;
    }

    /**
     * @param board0  eigenes Spielfeld
     * @param board1  Spielfeld mit eigenen Schüssen
     * @param myTurn  Boolean der angibt ob ich oder Gegner gespeichert hat
     * @param myLifes Eigene Leben
     * @param lifes   Gegnerische Leben
     *                Speichert aktuellen Spielstand bei gegebener ID
     * @return false im Fehlerfall
     */
    public static boolean save(String[][] board0, String[][] board1, boolean myTurn, int myLifes, int lifes) {
        String name = "" + System.currentTimeMillis();
        try {
            Launcher.opponent.sendSave(Long.valueOf(name));
            return saveWithID(board0, board1, name, myTurn, myLifes, lifes);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param name ID der zu ladenden Datei
     *             Lädt gespeicherten Spielstand
     * @return true, wenn Gegner dran kommt
     */
    public static boolean load(String name) {
        String line = "";
        String[] myLine;
        int myLifes = 0, lifes = 0, size = 0;
        boolean myTurn = false; //Rückgabewert der angibt ob ich oder Gegner am Zug
        String[][] board0 = null;
        String[][] board1 = null;
        String[][] board2 = null;
        String[][] board3 = null;
        int counter = 0;
        int propertyCount = 1; //Anzahl der gelesen KI properties
        boolean firstBoardsFinished = false; //Wird true, wenn Spielfelder beide eingelesen und noch KI Infos kommen
        boolean secondBoardsFinished = false; //Wird true, wenn Spielfelder alle eingelesen und noch KI Infos kommen
        Opponent_AI ki = null;
        if (((Player) Launcher.player).opponent instanceof Opponent_AI)
            ki = ((Opponent_AI) ((Player) Launcher.player).opponent);
        try (
                BufferedReader reader = new BufferedReader(new FileReader(name + ""))) {

            while ((line = reader.readLine()) != null) {
                if (counter == 0 && !firstBoardsFinished) {
                    myLine = line.split(",");
                    size = Integer.valueOf(myLine[0]);
                    myTurn = Boolean.valueOf(myLine[1]);
                    myLifes = Integer.valueOf(myLine[2]);
                    lifes = Integer.valueOf(myLine[3]);
                    board0 = new String[size][size];
                    board1 = new String[size][size];
                    board2 = new String[size][size];
                    board3 = new String[size][size];

                }
                if (counter > 0 && counter < size + 1 && !firstBoardsFinished) {
                    myLine = line.split(",");
                    board0[counter - 1] = myLine;
                }
                //zweites Spielfeld einlesen
                else if (counter > size + 1 && counter < size * 2 + 2 && !firstBoardsFinished) {
                    myLine = line.split(",");
                    board1[(counter - 2) % size] = myLine;
                } else if (counter == size * 2 + 2 && line.split(",")[0].equals("SINGLE PLAYER"))
                    firstBoardsFinished = true;
                    //Drittes Spielfeld einlesen
                else if (counter > size * 2 + 2 && counter < size * 3 + 3 && firstBoardsFinished) {
                    myLine = line.split(",");
                    board2[(counter - 3) % size] = myLine;
                } else if (counter == size * 3 + 3 && line.split(",")[0].equals("SINGLE PLAYER2")) {
                }
                // secondBoardsFinished=true;
                else if (counter > size * 3 + 3 && counter < size * 4 + 4) {
                    myLine = line.split(",");
                    board3[(counter - 4) % size] = myLine;
                } else if (counter == size * 4 + 4)
                    secondBoardsFinished = true;

                else if (secondBoardsFinished) {  //Einlesen der KI Properties
                    String single = line.split(",")[0];
                    switch (propertyCount) {
                        case 1:
                            ki.x = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 2:
                            ki.y = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 3:
                            ki.firstX = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 4:
                            ki.firstY = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 5:
                            ki.i = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 6:
                            ki.j = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 7:
                            ki.counter = Integer.parseInt(single);
                            propertyCount++;
                            break;
                        case 8:
                            ki.result = single;
                            propertyCount++;
                            break;
                        case 9:
                            ki.rotation = single;
                            propertyCount++;
                            break;
                        case 10:
                            ki.uncovered = Boolean.valueOf(single);
                            propertyCount++;
                            break;
                        case 11:
                            ki.localized = Boolean.valueOf(single);
                            propertyCount++;
                            break;
                        case 12:
                            ki.sank = Boolean.valueOf(single);
                            propertyCount++;
                            break;
                        case 13:
                            ki.even = Boolean.valueOf(single);
                            propertyCount++;
                            break;
                    }
                }
                counter++;
            }
            //Aufräumen der Felder (Entfernen der Punkte)
            for (int i = 0; i < board0.length; i++) {
                for (int j = 0; j < board0.length; j++) {
                    if (board0[i][j].equals(".")) board0[i][j] = null;
                    if (board1[i][j].equals(".")) board1[i][j] = null;
                    if (board2[i][j] != null) { //Wenn überhaupt ein KI Spiel geladen wird
                        if (board2[i][j].equals(".")) board2[i][j] = null;
                        if (board3[i][j].equals(".")) board3[i][j] = null;
                    }
                }
            }
            Launcher.player.setSize(size + "");
            Launcher.opponent.setSize(size + "");
            SuperController.setTableSize(size, 950.0 / size);
            Launcher.player.myBoard = board0;
            Launcher.player.opBoard = board1;
            Launcher.player.myLifes = myLifes;
            Launcher.player.lifes = lifes;
            if (ki != null) { //Wenn überhaupt ein KI Spiel geladen wird
                ki.myBoard = board2;
                ki.opBoard = board3;
            }

            Viewer.drawBoard(board0, board1);
            Viewer.drawBoard(board2, board3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myTurn;
    }

}

