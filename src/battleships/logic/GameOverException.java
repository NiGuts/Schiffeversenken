package battleships.logic;

/**
 * Wird geworfen, wenn Spieler gewonnen oder Verloren hat
 */
public class GameOverException extends Exception {
    public static boolean winner;
    public static String foo; //Dummy Objekt um zu gucken ob schonmal aufgerufen

    GameOverException(boolean winner) {
        super();
        this.winner = winner;
        this.foo = "foo";
        if (Launcher.opponent instanceof Op_Player) { //Wenn imm Mehrspielermodus oder KI gegen KI
            ((Op_Player) Launcher.opponent).closeSocket();
        }
    }
}
