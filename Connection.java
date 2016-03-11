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
	
	/**
	 * return the nature of this object. true if it is a server side socket,
	 * false if it is a client side socket
	 */
	public boolean isServer() {
		return isServer;
	}
	
	/**
	 * Send command to the remote host. It throws IOExceptoin is caught for broken pipe
	 */
	public void sendCommand(String command) throws Exception{
		oos.writeObject(command);
	}
	
	/**
	 * Send the file name list to the remote host. It throws IOExceptoin is caught for broken pipe
	 */
	public void sendFileNameList(Vector<String> fileNameList) throws Exception{
		oos.writeObject(fileNameList);
	}
	
	/**
	 * Send the file list to the remote host. It throws IOExceptoin for broken pipe
	 */
	public void sendFileList(Vector<File> fileList) throws Exception {
			oos.writeObject(fileList);
	}
	
	/**
	 * Reads file name list from the pipe. Returns all the String objects 
	 * received from the pipe. Throws Exception when the pipe is broken or 
	 */
	public Vector<String> receivedFileNameList() throws IOException, ClassNotFoundException{
		Vector<String> fileNameList = new Vector<String>();
		Object received = ois.readObject();
		if (received instanceof Vector<?>)
			for (Object o: (Vector<?>) received)
				if (o instanceof String)
					fileNameList.add((String) o);
					
		return fileNameList;	
	}
	
	/**
	 * Reads file list from the pipe. Returns all the File objects 
	 * received from the pipe. Throws Exception when the pipe is broken or 
	 */
	public Vector<File> receivedFileList() throws IOException, ClassNotFoundException{
		Vector<File> fileList = new Vector<File>();
		Object received = ois.readObject();
		if (received instanceof Vector<?>)
			for (Object o: (Vector<?>) received)
				if (o instanceof File)
					fileList.add((File) o);
					
		return fileList;	
	}
	
	/**
	 * Reads the command in string from remote host
	 */
	public String receiveCommand() throws IOException, ClassNotFoundException {
		Object received = ois.readObject();
		if (received instanceof String)
			return (String) received;
		else
			return "";
	}
	/**
	 * Return the IP of the remote host
	 */
	public String getRemoteAddress() {
		return s.getInetAddress().toString();
	}
	
	/**
	 * Safely close the connection
	 */
	public void closeConnection() {
		try {
			oos.close();
			ois.close();
		    s.close();
		} catch (IOException e) {
			//System.out.println("Unable to close connection, probably lost");
		}
	}
}
