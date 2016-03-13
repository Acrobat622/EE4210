/**
 * The main class for file synchronization
 * 
 *
 */
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class FileSync {
	private static final String SYNC = "SYN";
	private static final String REQUEST = "REQ";
	private static final String EXIT = "EXIT";
	private static final String SWITCH = "SWITCH";
	private static final int PORT = 6883;
	private static boolean isServer = true;
	private static String remote;
	private static final File path = new File(FileSync.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	private static Connection connection;
	private static FileOperator fo;
	private static Vector<String> fileNameList = new Vector<String>();
	
	public static void main(String[] args) {
		checkArguments(args);
		printWelcomeMessage(args);
		
		fo = new FileOperator(getWorkingDirectory());
		fileNameList = fo.getFileNameList();
		//while (true) {
			try {
				if (isServer)
					createNewConnection();
				else
					createNewConnection(remote);
				System.out.println("Connected to " + connection.getRemoteAddress());
			} catch (IOException e) {
			
			}

		
		// stub 
		if (connection.isServer()) {
			server();
		}
		else
			client();
		//}
	}

	/**
	 * Return the path where the file is running in String
	 */
	private static String getWorkingDirectory() {
		return FileSync.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	private static void server() {
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
					case SWITCH:
						client();
						break;
					case EXIT:
						connection.closeConnection();
					default:
						alive = false;	
							
				}
			} catch (IOException e) {
				System.out.println("Connection lost, closing connection");
			} catch (ClassNotFoundException cnfe) {
				
			} finally {
			}
		}
	}
	
	private static void client() {
		Vector<String> missingFileNameList = new Vector<String>();
		Vector<File> missingFile = new Vector<File>();
		boolean serverMissing = true;
		try {
			connection.sendCommand(REQUEST);
			missingFileNameList.addAll(connection.receivedFileNameList());
			//missingFileNameList.removeAll(fo.getFileNameList());
			for (String s: fo.getFileNameList()) 
				serverMissing = serverMissing & missingFileNameList.remove(s);
			connection.sendFileNameList(missingFileNameList);
			
			connection.sendCommand(SYNC);
			connection.receivedFiles(path);
			
			if (serverMissing) {
				connection.sendCommand(EXIT);
				connection.closeConnection();
			}
			else {
				connection.sendCommand(SWITCH);
				server();
			}
		} catch (IOException e) {
			e.printStackTrace();
			connection.closeConnection();
			System.out.println("Connection lost when receiving file name list, closing connection");
		} catch (ClassNotFoundException cnfe) {
			
		} finally {
			System.out.println("Received " + missingFile.size() + " files from remote");
		}
	}

	/**
	 * Creates new server connection  specifying the remote address
	 * If the remote host is unreachable, it will throw exception 
	 */	 
	private static void createNewConnection(String remote) throws IOException{
		try {
			   connection = new Connection(remote, PORT);
			} catch (IOException e) {
				System.out.println("Unable to connect to " + remote +" at port " + PORT );
			}
	}
	/**
	 * Creates new server connection without specifying the remote address
	 * If the remote host is unreachable, it will throw exception 
	 */	 
	private static void createNewConnection() throws IOException{
		try {
			connection = new Connection(PORT);
		} catch (Exception e) {
			System.out.println("Unable to bind port " + PORT + ", exiting");
		}
	}

	private static void printWelcomeMessage(String[] args) {
	   System.out.println("Welcome to File Sync.");
	   System.out.println("This programme will try to synchronize the files in current directory with another host");	
	   if (isServer) {
		   System.out.println("No remote host is specified.");
	   	   System.out.println("Serving as server at port " + PORT);
	   }
	   else
		   System.out.println("Remote host is " + remote + ". Trying to establish connetion");
	}

	// check if the programme runs with remote host IP
    // only takes the first argument as remote host 
	private static void checkArguments(String[] args) {
		if (args.length == 0)
			isServer = true;
		else {
			remote = args[0];
			isServer = false;
		}
	}
}
