/**
 * The Server process of file synchroninzation
 * Server process awaits and establishes the coonection with a
 * remote host, collects the files in its folder, send file names 
 * to the client host, send files that client host. 
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;

public class ServerProcess {
	private static String host;
	private static int port;
	private static File path;
	private Vector<File> fileList;
	private Vector<String> fileNameList;
	private ServerSocket ss;
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	public ServerProcess(String host, int port, File path) {
		this.host = host;
		this.port = port;
		this.path = path;
		this.fileList = new Vector<File>();
		this.fileNameList = new Vector<String>();
	}
	
	public void getFileList() {
		this.fileList.addAll(Arrays.asList(path.listFiles()));
	}
	
	public void getFileNameList() {
		for (File f: fileList) {
			fileNameList.add(f.getName());
		}
		
	}
	
}
