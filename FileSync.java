/**
 * The main class for file synchronization
 * It allows user to specify which mode to enter (server or client)
 * and runs the respective methods
 *
 */
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Scanner;

public class FileSync {
	private static final String SYNC = "SYN";
	private static final String REQUEST = "REQ";
	private static final String EXIT = "EXIT";
	private static final String SEND = "SEND";
	private static final int PORT = 6883;
	private static Connection connection;
	private static FileOperator fo;
	private static Vector<String> fileNameList = new Vector<String>();
	
	public static void main(String[] args) {
		//checkArguments(args);
		printWelcomeMessage();
		
		fo = new FileOperator(getWorkingDirectory());
		fileNameList = fo.getFileNameList();
		Scanner sc = new Scanner(System.in);
		while (true) {
			printInstruction();
			String command = sc.nextLine().trim();
			switch (command) {
				case "":
					try {
						System.out.println("Working as server at port "+ PORT +". Press Ctrl^C to quit");
						createServerSocket();
						System.out.println("Connected to " + connection.getRemoteAddress());
						server();
					} catch (IOException e) {
						System.out.println("Unable to bind port " + PORT +". Is another instance using?" );
						continue;
					}
					break;
				case "exit":
					System.out.println("Goodbye!");
					sc.close();
					break;
				default:
					try {
						createClientSocket(command);
						System.out.println("Connected to " + connection.getRemoteAddress());
						client();
					}  catch (IOException e) {
						System.out.println("Unable to connect to " + command +" at port " + PORT );
						continue;
					}
			}
		}
	}

	/**
	 * Return the path where the file is running in String
	 */
	private static String getWorkingDirectory() {
		return FileSync.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	private static void server() {
		int missing = 0;
		boolean alive = true;
		Vector<String> requestedFileNameList = new Vector<String>();
		while (alive) {
			try {
				String command = connection.receiveCommand();
				switch (command) {
					case REQUEST:
						connection.sendFileNameList(fileNameList);
						requestedFileNameList.addAll(connection.receivedFileNameList());
						break;
					case SYNC:
						Vector<File> requestedFileList = fo.prepareRequestedFile(requestedFileNameList);
						connection.sendFiles(requestedFileList);
						break;
					case SEND:
						//connection.sendAck();
						missing = connection.receiveFiles();
						break;
					case EXIT:
						connection.closeConnection();
					default:
						alive = false;	
							
				}
			} catch (IOException e) {
				System.err.println("Connection lost, closing connection");
				alive = false;
			} catch (ClassNotFoundException cnfe) {
				System.err.println("Error processing data types");
			} finally {
				System.out.println("Sent " + requestedFileNameList.size() + " files to remote");	
				System.out.println("Received " + missing + " files from remote");
			}
		}
	}
	
	private static void client() {
		Vector<String> localMissingFileNameList  = new Vector<String>();
		Vector<File> remoteMissingFileList = new Vector<File>();
		int localMissing = 0;
		boolean serverMissing = true;
		try {
			connection.sendCommand(REQUEST);
			localMissingFileNameList.addAll(connection.receivedFileNameList());
			//missingFileNameList.removeAll(fo.getFileNameList());
			for (String s: fo.getFileNameList()) {
				boolean removed = localMissingFileNameList.remove(s);
				serverMissing = serverMissing & removed;
				if (!removed)
					remoteMissingFileList.add(new File(fo.getPath() + File.separator + s));
				//System.out.println(remoteMissingFileList);
			}
			localMissing = localMissingFileNameList.size();
			connection.sendFileNameList(localMissingFileNameList);
			
			if (!localMissingFileNameList.isEmpty()) {
				connection.sendCommand(SYNC);
				connection.receiveFiles();
			}
			
			if (serverMissing) {
				connection.sendCommand(EXIT);
				connection.closeConnection();
			}
			else {
				connection.sendCommand(SEND);
				//if (connection.recvAck())
					connection.sendFiles(remoteMissingFileList);
				//else
				//	connection.closeConnection();
			}
		} catch (IOException e) {
			e.printStackTrace();
			connection.closeConnection();
			System.err.println("Connection lost, closing connection");
		} catch (ClassNotFoundException cnfe) {
			System.err.println("Error processing data types");
		} finally {
			System.out.println("Received " + localMissing + " files from remote");
			System.out.println("Sent " + remoteMissingFileList.size() + " files to remote");
		}
	}

	/**
	 * Creates new server connection  specifying the remote address
	 * If the remote host is unreachable, it will throw exception 
	 */	 
	private static void createClientSocket(String remote) throws IOException{
		try {
			   connection = new Connection(remote, PORT, fo);
			} catch (IOException e) {
				//System.out.println("Unable to connect to " + remote +" at port " + PORT );
				throw new IOException("No route to host");
			}
	}
	/**
	 * Creates new server connection without specifying the remote address
	 * If the remote host is unreachable, it will throw exception 
	 */	 
	private static void createServerSocket() throws IOException{
		try {
			connection = new Connection(PORT, fo);
		} catch (Exception e) {
			System.out.println("Unable to bind port " + PORT + ", exiting");
		}
	}

	private static void printWelcomeMessage() {
	   System.out.println("Welcome to File Sync.");
	   System.out.println("This programme will try to synchronize the files in current directory with another host");	
	}

	private static void printInstruction() {
		System.out.println();
		System.out.println("Press Enter without entering anything or leaving blank enters server mode");
		System.out.println("Type remote host IP or hostname to sync file with another host");
	    System.out.println("Type 'exit' to quit");
	}

}
