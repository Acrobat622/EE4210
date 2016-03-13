/**
 * Connection with other server
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
	private FileOperator fo;
	
	// client constructor
	public Connection(String host, int port, FileOperator fo) throws IOException {
		this.fo = fo;
		s = new Socket();
		s.connect(new InetSocketAddress(host, port)/*, TIMEOUT*/);
		isServer = false;
		//s.setSoTimeout(TIMEOUT);
		oos = new ObjectOutputStream(s.getOutputStream());
		ois = new ObjectInputStream(s.getInputStream());
	}
	
	// server constructor
	public Connection(int port, FileOperator fo) throws IOException {
		this.fo = fo;
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
	 * Send the files to the remote host. It throws IOExceptoin for broken pipe
	 * An ACK is required for each file sent. It sets the timeout for receiving 
	 * ACK to 1s
	 */
	public void sendFiles(Vector<File> fileList) throws IOException, ClassNotFoundException {
			//s.setSoTimeout(TIMEOUT);
			oos.writeObject(fileList.size());  // send how many files are to be sent
			
			for (File f: fileList) {
				oos.writeObject(f.getName());  // send the name of file
				oos.writeObject(f.length()); // send the size of file
				byte[] fileBytes = Files.readAllBytes(f.toPath());
				//oos.write(fileBytes);
				oos.writeObject(fileBytes);
				if (!recvAck())
					break;
			}
			//s.setSoTimeout(0); // reset timeout
	}
	
	/**
	 * Reads file list from the pipe. Returns all the File objects 
	 * received from the pipe and write them to disk. 
	 * Throws Exception when the pipe is broken
	 */
	@SuppressWarnings("finally")
	public int receiveFiles() {
		int fileCount = 0;
		try {
			Object fileNumber = ois.readObject();
			for (int i = 0; i < (int)fileNumber; i++) {
				String name = ois.readObject().toString();
				long size = (long)ois.readObject();
				byte[] bytes = new byte[(int) size];
				ois.read(bytes);
			
				fo.writeFilesToDisk(name, bytes);
				fileCount++;
				sendAck();
			}
		} catch (IOException|ClassNotFoundException e) {
			System.err.println("Error writing file to disk");
		} finally {
			return fileCount;	
		}
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
	public void sendAck() throws IOException {
		oos.writeObject(ACK);
	}
	
	/**
	 * Read and check if ACK is received
	 */
	public boolean recvAck() throws IOException, ClassNotFoundException {
		Object response = ois.readObject();
		if (response.toString().equals(ACK))
			return true;
		else
			return false;
	}
	
	public void flush() throws IOException {
		oos.flush();
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
