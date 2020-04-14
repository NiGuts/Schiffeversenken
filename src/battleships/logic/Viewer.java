package battleships.logic;

/**
 *
 */
public class Viewer {
    static String defaultColor = "\u001B[0m";
    static String color = defaultColor;
    public static boolean disabled = false;

    /** Mtehode um Spielfelder zu zeichnen
     * @param board erstes Spielfeld
     * @param opBoard zweites Spielfeld
     */
    static public void drawBoard(String[][] board, String[][] opBoard) {  //int[][] opBoard
        if (disabled)
            return;
        String[][] currentBoard = board;
        for (int x = 0; x < 2; x++) {
            System.out.print(" ");
            for (int i = 0; i < board.length; i++)
                System.out.print("  " + i + "    ");
        }
        System.out.println();
        for (int x = 0; x < 2; x++) {
            for (int i = 0; i < board.length; i++)
                System.out.print("  ---  ");
            System.out.print(" ");
        }
        System.out.println();

        for (int i = 0; i < board.length; i++) {
            for (int x = 0; x < 2; x++) {
                if (x == 1) currentBoard = opBoard;
                if (x == 0) currentBoard = board;
                for (int j = 0; j < board.length; j++) {
                    if (currentBoard[i][j] != null) {  // Wenn es etwas anzuzeigen gibt
                        if ((currentBoard == opBoard) || currentBoard == board)
                        //Sofern es sich nicht um ein gegnerisches Schiff handelt
                        {
                            if (currentBoard[i][j].equals("X")) {
                                color = "\u001B[31m";
                                System.out.print(defaultColor + " | " + color + "X" + defaultColor + " | ");
                            } else if (currentBoard[i][j].equals("w")) {
                                System.out.print(defaultColor + " | " + "\u001B[36m" + currentBoard[i][j] + defaultColor + " | ");
                            } else {
                                System.out.print(defaultColor + " | " + "\u001B[32m" + currentBoard[i][j] + defaultColor + " | ");
                            }
                        } else {
                            System.out.print(" |   | ");
                        }
                    } else {
                        System.out.print(" |   | ");
                    }
                }
                System.out.print("" + i);
            }
            System.out.println();
            for (int z = 0; z < board.length; z++)
                System.out.print("  ---  ");
            System.out.print(" ");
            for (int z = 0; z < board.length; z++)
                System.out.print("  ---  ");
            System.out.println();
        }
        System.out.println("DRAW BOARD");
    }

    /**
     * Konsolenausgabe löschen
     */
    public void clearConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (Exception e) {

        }

    }
}