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

public class FileOperator {
	private static File path;
	private Vector<File> fileList;
	private Vector<String> fileNameList;
    private Connection connection;
	
	public FileOperator(File path, Connection connection) {
		this.connection = connection;
		this.path = path;
		this.fileList = new Vector<File>();
		this.fileNameList = new Vector<String>();
	}
	
	public void getFileList() {
		this.fileList.addAll(Arrays.asList(path.listFiles()));
	}
	
	/**
	 * return true if filename list is sent successfully; false if 
	 * IOException is caught
	 */
	public boolean sendFileNameList() {
		try {
			connection.sendFileNameList(fileNameList);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	/**
	 * return true if files are sent successfully; false if IOException is caught
	 */
	public boolean sendFileList() {
		try {
			connection.sendFileList(fileList);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	
	
	public void getFileNameList() {
		for (File f: fileList) {
			fileNameList.add(f.getName());
		}
		
	}
	
}
