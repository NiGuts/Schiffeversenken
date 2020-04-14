package battleships.logic;

import battleships.network.Client;
import battleships.network.NetzwerkImpl;
import battleships.network.Server;

import java.io.IOException;

/**
 * Wrapperklasse für das Netzwerk
 */
public class Op_Player extends GameObject {
    NetzwerkImpl s;
    Thread t2;
    Thread t1;
    int size;
    String ip;

    /**
     * Konstruktor für Server.
     *
     * @param size
     * @throws IOException
     */
    public Op_Player(int size) throws IOException {  // Konstruktor für Server
        this.s = new Server();
        Server server = (Server) s;
        this.size = size;
    }

    /**
     * Konstruktor für Client.
     *
     * @param ip
     */
    public Op_Player(String ip) {  // Konstruktor für Client
        this.s = new Client();
        Client client = (Client) s;
        this.ip = ip;

    }
    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     */
    public void closeSocket() {
        try {
            s.closeSocket();
        } catch (IOException e) {
            System.err.println("Gegner hat die Verbindung bereits getrennt");
        }
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @throws IOException
     */
    public void sendReady() throws IOException {
        s.sendReady();
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @param x Koordinate X.
     * @param y Koordinate Y.
     * @throws IOException
     */
    public void sendShot(String x, String y) throws IOException {
        s.sendShot(Integer.valueOf(x), Integer.valueOf(y));
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @param id ID der Datei.
     * @throws IOException
     */
    public void sendSave(long id) throws IOException {
        s.sendSave(id);
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @param id
     * @throws IOException
     */
    public void sendLoad(long id) throws IOException {
        s.sendLoad(id);
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @throws IOException
     */
    public void sendPass() throws IOException {
        s.sendPass();
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @throws IOException
     */
    public void expectConnection() throws IOException {
        ((Server) s).expectConnection();
        t1 = new Thread((Server) s) {
        };
        t1.start();
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     */
    public void sendFieldsize() {
        try {
            ((Server) s).sendFieldSize(super.size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @throws IOException
     */
    public void connectToServer() throws IOException {
        ((Client) s).connectToServer(ip);
        t2 = new Thread((Client) s) {
        };
        t2.start();
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @param value
     * @throws IOException
     */
    public void sendAnswer(int value) throws IOException {
        s.sendAnswer(value);
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @throws IOException
     */
    public void expectInput() throws IOException {

        s.expectInput();
    }

    /**
     * Wrapper Methode für die entsprechende Netzwerkmethode.
     *
     * @return
     */
    public String[] getBufferedElement() {

        return s.getBufferedElement();
    }

}