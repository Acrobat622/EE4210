/**
 * The main class for file synchronization
 * 
 *
 */
import java.io.File;

public class FileSync {

	public static final int PORT = 6883;
	private static boolean isServer = true;
	private static String remote;
	private static final File path = new File(FileSync.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	private static Connection connection;
	
	public static void main(String[] args) {
		checkArguments(args);
		printWelcomeMessage(args);
		
		
		if (!isServer)
			try {
			   connection = new Connection(remote, PORT);
			   System.out.println("Connected to " + connection.getRemoteAddress());
			} catch (Exception e) {
				try {
				    connection = new Connection(PORT);
				} catch (Exception e2) {
					System.out.println("Unable to bind port, exiting");
					System.exit(1);
				}
			}
		else
			try {
			    connection = new Connection(PORT);
			} catch (Exception e) {
				System.out.println("Unable to bind port, exiting");
				System.exit(1);
			}
		
	}

	private static void printWelcomeMessage(String[] args) {
	   System.out.println("Welcome to File Sync.");
	   System.out.println("This programme will try to synchronize the files in current directory with another host");	
	   if (isServer) 
		   System.out.println("No remote host is specified. Awaiting connection from another host.");
	   else
		   System.out.println("Remote host is " + remote + ". Trying to establish connetion");
	}

	// check if the programme runs with remote host IP
    // only takes the first argument as remote host 
	private static void checkArguments(String[] args) {
		if (args.length == 0)
			isServer = true;
		else {
			remote = args[1];
			isServer = false;
		}
	}
}
