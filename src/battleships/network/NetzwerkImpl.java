package battleships.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Diese Klasse definiert Methode, welche beide vom Server und vom Client benutzt werden.
 */

public abstract class NetzwerkImpl implements Runnable {

    public Writer out = null;
    protected BufferedReader in = null;
    protected String lastEntry = "";
    protected int counter = 0;
    protected final int port = 50000;
    Socket s = null;
    private int offset = 0;
    ServerSocket ss;   //Test

    /**
     * Eingehende Nachrichten werden nacheinander in den Buffer geschrieben
     */
    private ArrayList<String[]> buffer = new ArrayList<>();

    /**
     * Sendet eine "Shot"-Nachricht mit der übergebenen Zeile und Spalte an den Gegner.
     *
     * @param row    Zeile des Spielfelds
     * @param column Spalte des Spielfelds
     * @throws IOException              falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     * @throws IllegalArgumentException falls Zeile oder Spalte kleiner 0 ist
     */
    public void sendShot(int row, int column) throws IOException, IllegalArgumentException {
        row += offset;
        column += offset;
        if (row < 0 || column < 0)
            throw new IllegalArgumentException("Zeile/Spalte darf nicht kleiner 0 sein");
        outputStreamAvailable();
        out.write(String.format("shot %d %d\n", row, column));
        out.flush();
    }

    /**
     * Liest nacheinander die Nachrichten im Buffer aus und gibt diese zurück.
     *
     * @return enthält die aufgespaltene Nachricht - Bsp.: aus "shot 2 5" wird "shot" "2" "5"
     */
    public String[] getBufferedElement() {
        if (buffer.size() >= counter + 2) { //Normalerweise sollte der Buffer immer höchstens um ein Element gröüer sein als der Counter
            System.err.println("Eine ungültige Eingabe wurde gelöscht!");
            buffer.remove(buffer.size() - 1);
        }
        while (buffer.size() == counter) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.err.println("Wer störet mich in meinem Schlafe?");
            }
        }
        System.out.println(buffer.get(buffer.size() - 1)[0] + " ist das neue und " + lastEntry + " ist das letzte. Aktuelle Größe: " + buffer.size());
        if ((lastEntry.equals("pass") && buffer.get(buffer.size() - 1)[0].equals("pass")))
            System.err.println("Schwerwiegender Fehler");
        lastEntry = buffer.get(buffer.size() - 1)[0];
        counter++;

        System.err.println("Buffer: " + Arrays.deepToString(buffer.toArray()));
        return buffer.get(buffer.size() - 1);
    }

    /**
     * Sendet eine "Ready"-Nachricht an den Gegner.
     *
     * @throws IOException falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     */
    public void sendReady() throws IOException {
        outputStreamAvailable();
        out.write("confirmed\n");
        out.flush();
    }

    /**
     * Sendet eine "Pass"-Nachricht an den Gegner.
     *
     * @throws IOException falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     */
    public void sendPass() throws IOException {
        outputStreamAvailable();
        out.write("pass\n");
        out.flush();
    }

    /**
     * Sendet eine "Answer"-Nachricht an den Gegner.
     *
     * @param value den Wert der Antwort (0, 1 oder 2)
     * @throws IOException              falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     * @throws IllegalArgumentException falls "value" nicht 0, 1 oder 2
     */
    public void sendAnswer(int value) throws IOException, IllegalArgumentException {
        if (value != 0 && value != 1 && value != 2)
            throw new IllegalArgumentException(String.format("Wert darf 0,1 oder 2 sein. Uebergebener Wert %d", value));
        outputStreamAvailable();
        out.write(String.format("answer %d\n", value));
        out.flush();
    }

    /**
     * Schließt den ServerSocket und den Socket, falls diese jeweils vorhanden sind.
     *
     * @throws IOException falls mindestens ein Socket nicht geschlossen verden kann.
     */
    public void closeSocket() throws IOException { //Fr DEBUG Zwecke
        if (ss != null) {
            ss.close();
        }
        if (s != null) {
            s.close();
        }
    }

    /**
     * Sendet eine "Save"-Nachricht an den Gegner.
     *
     * @param id die Save-ID, welche dem Gegener zur Speicherung übermittelt wird.
     * @throws IOException falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     */
    public void sendSave(long id) throws IOException {//Wert als Parametrer
        outputStreamAvailable();
        out.write("save " + id + "\n");
        out.flush();
    }

    /**
     * Sendet eine "Load"-Nachricht an den Gegner.
     *
     * @param id die ID der gesaveten Datei, welche geladen werden soll.
     * @throws IOException falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden
     */
    public void sendLoad(long id) throws IOException {//Wert als Parametrer
        outputStreamAvailable();
        out.write("load " + id + "\n");
        out.flush();
    }

    /**
     * Prüft, ob ein Outputstream vorhanden ist
     *
     * @throws IOException falls kein Outputstream vorhanden
     */
    protected void outputStreamAvailable() throws IOException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Wer störet mich in meinem Schlafe?");
        }
        if (out == null)
            throw new IOException("Keine Netzwerkverbindung vorhanden");
    }

    /**
     * Wartet auf eine Verbindung zum Socket. Wird in einem Thread parallel ausgeführt.
     */
    public void run() {
        try {
            expectInput();
        } catch (IOException e) {
            System.err.println("Gegner hat das Spiel beendet");
        }
    }

    /**
     * Liest empfangene Nachrchten aus dem Inputstream aus und schreibt diese in den Buffer.
     *
     * @throws IOException falls Inputstream nicht vorhanden oder lesen aus Inputstream nicht möglich
     */
    public void expectInput() throws IOException {
        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Wer störet mich in meinem Schlafe?");
        }
        String line;
        String[] split;
        while (true) {
            if (out != null && in != null) {
                line = in.readLine();
                if (line != null) {
                    split = line.split(" ");
                    switch (split[0].toLowerCase()) {
                        case "shot":

                            String[] s_l3 = {split[0], "" + (Integer.parseInt(split[1]) - offset), "" + (Integer.parseInt(split[2]) - offset)};
                            buffer.add(s_l3);
                            break;
                        case "answer":
                        case "save":
                        case "load":
                        case "size":
                            String[] s_l2 = {split[0], split[1]};
                            buffer.add(s_l2);
                            break;
                        case "confirmed":
                        case "pass":
                            String[] s_l1 = {split[0]};
                            buffer.add(s_l1);
                            break;
                        case "close": //Case f�r DEBUG Zwecke
                            closeSocket();
                            return;
                        default:
                            System.err.println("Unzulaessige Nachricht: " + line);
                    }
                }
            }
        }
    }
}


