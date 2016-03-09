/**
 * Connectionn with other server
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Connection {
	private static String host;
	private static int port;
    private Socket s;
    private ServerSocket ss;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	// client constructor
	public Connection(String host, int port) throws Exception {
			s = new Socket(host, port);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
	}
	
	// server constructor
	public Connection(int port) throws Exception {
		ss = new ServerSocket(port);
		s = ss.accept();
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());
	}
	
	public void sendMessage(String message) throws IOException {
		oos.writeObject(message);
	}
	
	public String receiveMessage() {
		Object received = null;
		try {
			received = ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println(e.getMessage());
		}
		
		if (received instanceof String) 
			return (String) received;
		else
			return "";
	}
	
	public String getRemoteAddress() {
		return s.getInetAddress().toString();
	}
	
	public void closeConnection() {
		try {
		    s.close();
		} catch (IOException e) {
			System.out.println("Unable to close connection, probably lost");
		}
	}
}
