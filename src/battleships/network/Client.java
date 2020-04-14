package battleships.network;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Definiert die Methoden für den Client
 */
public class Client extends NetzwerkImpl {

	/**
	 * Erstellt einen Socket und verbindet sich zur übergebenen IP-Adresse
	 *
	 * @param ip IP-Adresse zu der sich verbunden werden soll.
	 * @throws IOException Falls die Verbindung fehlschlägt.
	 */
	public void connectToServer(String ip) throws IOException {
		s = new Socket(ip, port);
		s.setReuseAddress(true);
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
		System.out.println("Connection established.");
	}
	
}
