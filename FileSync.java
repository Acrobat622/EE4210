/**
 * The main class for file synchronization
 * 
 *
 */
import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class FileSync {

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
		createNewConnection();

		System.out.println("Connected to " + connection.getRemoteAddress());
		
		fileNameList = fo.getFileNameList();
		// stub 
		if (connection.isServer()) {
			server();
		}
		else
			client();
	}

	/**
	 * Return the path where the file is running in String
	 */
	private static String getWorkingDirectory() {
		return FileSync.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	}

	private static void server() {
		try {
			connection.sendFileNameList(fileNameList);
		} catch (Exception e) {
			System.out.println("Connection lost, closing connection");
			connection.closeConnection();
		}
	}
	
	private static void client() {
		try {
			fileNameList = connection.receivedFileNameList();
			System.out.println(fileNameList);
		} catch ( ClassNotFoundException | IOException e) {
			connection.closeConnection();
			System.out.println("Connection lost, closing connection");
		}
	}
	/**
	 * Creates new connection, whether it is a server or client socket
	 * If the programme is served as a client and the connection cannot
	 * be established after the timeout, it will enter server mode which 
	 * listens to connection requests
	 */	 
	private static void createNewConnection() {
		if (!isServer)
			try {
			   connection = new Connection(remote, PORT);
			} catch (IOException e) {
				System.out.println("Unable to connect to " + remote +" at port " + PORT );
				connection=null;
				try {
					System.out.println("Serving as server at port " + PORT);
				    connection = new Connection(PORT);
				} catch (IOException e2) {
					System.out.println("Unable to bind port " + PORT + ", exiting");
					System.exit(1);
				}
			}
		else
			try {
			    connection = new Connection(PORT);
			} catch (Exception e) {
				System.out.println("Unable to bind port " + PORT + ", exiting");
				System.exit(1);
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
