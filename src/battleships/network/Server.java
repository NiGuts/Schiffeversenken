package battleships.network;

import java.io.*;
import java.net.*;

/**
 * Definiert die Methoden für den Server
 */
public class Server extends NetzwerkImpl implements Runnable {

    /**
     * Erstellt ein neues ServerSocket-Objekt und bindet dieses an den Port
     *
     * @throws IOException falls Socket nicht erstellt werden kann oder falls Socket nicht an den Port gebunden werden kann
     */
    public Server() throws IOException {
        ss = new ServerSocket();
        ss.setReuseAddress(true);
        ss.bind(new InetSocketAddress(port));
    }

    /**
     * Wartet auf eine Verbindung eines Clients und erstellt das jeweilige Reader-Objekt zum In- und Outputstream
     *
     * @throws IOException     falls Reader-Objekt nicht erstellt werden kann
     * @throws SocketException falls Benutzer das Hosten Abbricht
     */
    public void expectConnection() throws IOException, SocketException {
        s = ss.accept();
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    }

    /**
     * Sendet eine "size"-Nachricht an den Gegner
     *
     * @param size Feldgröße (von 5 bis 30)
     * @throws IOException              falls senden der Nachricht fehlgeschlagen oder Socket nicht verbunden.
     * @throws IllegalArgumentException falls Feldgröße nicht zwischen 5 und 30 liegt.
     */
    public void sendFieldSize(int size) throws IOException, IllegalArgumentException {
        if (!(size >= 5 && size <= 30))
            throw new IllegalArgumentException("Die Groesse des Feldes muss zwischen 5 und 30 liegen!");
        outputStreamAvailable();
        out.write(String.format("size %d\n", size));
        out.flush();
    }

    /**
     * Ermittelt die IP-Adresse des Rechners
     *
     * @return die IP-Adresse als String
     * @throws SocketException Falls IP-Adresse nicht ausgelesen werden kann
     */
    public static String getIP() throws SocketException { //returns the local network IP-address of this machine or null if an Exception occurred

        String s = "";
        DatagramSocket socket = new DatagramSocket();

        try {
            socket.connect(InetAddress.getByName("10.10.10.10"), 1145);
            s = socket.getLocalAddress().getHostAddress();
        } catch (UnknownHostException e) {
        }

        return s;
    }

}
