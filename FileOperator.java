/**
 * The Server process of file synchronization
 * Server process awaits and establishes the coonection with a
 * remote host, collects the files in its folder, send file names 
 * to the client host, send files that client host. 
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;

public class FileOperator {
	private File path;
	private Vector<File> fileList;
	private Vector<String> fileNameList;
	
	public FileOperator(String path) {	
		this.path = new File(path);
		this.fileList = new Vector<File>();
		this.fileNameList = new Vector<String>();
		
		this.fileList.addAll(Arrays.asList(this.path.listFiles()));
		for (File f: fileList) {
			fileNameList.add(f.getName());
		}
	}
	
	/**
	 * Get the list of files in current directory
	 */
	public Vector<File> getFileList() {
		return fileList;
	}
	
	/**
	 * Get the list of names of files in current directory
	 */
	public Vector<String> getFileNameList() {
		return fileNameList;
	}
	
	/**
	 * Compare the missing files names from another host
	 */
	public Vector<String> getMissingFileNameList(Vector<String> remoteFileNameList) {
		Vector<String> requestedFileNameList = new Vector<String>();
		requestedFileNameList.addAll(fileNameList);
		requestedFileNameList.removeAll(remoteFileNameList);
		return requestedFileNameList;
	}
	
	/**
	 * Prepare the files to be sent to remote
	 */
	public Vector<File> prepareRequestedFile(Vector<String> requestedFileName) {
		Vector<File> preparedFile = new Vector<File>();
		for (File f: fileList) 
			if (requestedFileName.contains(f.getName()))
				preparedFile.add(f);
		return preparedFile;
	}
	
	public boolean writeFilesToDisk(String name, byte[] file) {
		try {
			File fout = new File(this.path + File.separator + name);
			FileOutputStream fos = new FileOutputStream(fout);
			fos.write(file);
			fos.close();
			return true;
		} catch (IOException ioe) {
			System.err.print("Error writing file " + name);
			return false;
		}
	}
	
	/**
	 * get the path of directory
	 */
	public String getPath() {
		return path.toString();
	}
}
