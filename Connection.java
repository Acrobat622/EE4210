/**
 * Connectionn with other server
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Connection {
	private static final int TIMEOUT = 1000;
	private static String host;
	private static int port;
	private static boolean isServer;
    private Socket s;
    private ServerSocket ss;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	// client constructor
	public Connection(String host, int port) throws IOException {
			s = new Socket();
			s.connect(new InetSocketAddress(host, port), TIMEOUT);
			isServer = false;
			//s.setSoTimeout(TIMEOUT);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
	}
	
	// server constructor
	public Connection(int port) throws IOException {
		ss = new ServerSocket(port);
		s = ss.accept();
		s.setSoTimeout(TIMEOUT);   //  timeout for connection
		isServer = true;
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());
	}
	
	public boolean isServer() {
		return isServer;
	}
	
	public void sendObject(Object obj) throws IOException {
		oos.writeObject(obj);
	}
	
	public Object receiveObject() {
		Object received = null;
		try {
			received = ois.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println(e.getMessage());
		}
		
		return received;
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
