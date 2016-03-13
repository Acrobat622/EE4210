/**
 * Connectionn with other server
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class Connection {
	private static final int TIMEOUT = 1000;
	private static final String ACK = "ACK";
	private static boolean isServer;
    private Socket s;
    private ServerSocket ss;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	// client constructor
	public Connection(String host, int port) throws IOException {
			s = new Socket();
			s.connect(new InetSocketAddress(host, port)/*, TIMEOUT*/);
			isServer = false;
			//s.setSoTimeout(TIMEOUT);
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
	}
	
	// server constructor
	public Connection(int port) throws IOException {
		ss = new ServerSocket(port);
		s = ss.accept();
		//s.setSoTimeout(TIMEOUT);   //  timeout for connection
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
	public void sendCommand(String command) throws IOException{
		oos.writeObject(command);
	}
	
	/**
	 * Send the file name list to the remote host. It throws IOExceptoin is caught for broken pipe
	 */
	public void sendFileNameList(Vector<String> fileNameList) throws IOException{
		oos.writeObject(fileNameList);
	}
	
	/**
	 * Send the files to the remote host. It throws IOExceptoin for broken pipe
	 * An ACK is required for each file sent. It sets the timeout for receiving 
	 * ACK to 1s
	 */
	public void sendFileList(Vector<File> fileList) throws IOException, ClassNotFoundException {
			s.setSoTimeout(TIMEOUT);
			oos.writeInt(fileList.size());  // send how many files are to be sent
			
			for (File f: fileList) {
				byte[] fileBytes = Files.readAllBytes(f.toPath());
				oos.write(fileBytes);
				sendAck();
				if (!recvAck())
					break;
			}
			s.setSoTimeout(0); // reset timeout
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
	 * received from the pipe and write them to disk. 
	 * Throws Exception when the pipe is broken
	 */
	public int receivedFileList() throws IOException, ClassNotFoundException{
		int size = ois.readInt();
		for (int i = 0; i < size; i++) {
			ois.readByte();
		}
					
		return 0;	
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
	 * Send ACK to remote
	 */
	private void sendAck() throws IOException {
		oos.writeObject(ACK);
	}
	
	/**
	 * Read and check if ACK is received
	 */
	private boolean recvAck() throws IOException, ClassNotFoundException {
		Object response = ois.readObject();
		if (response.toString().equals(ACK))
			return true;
		else
			return false;
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
