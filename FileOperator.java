/**
 * The Server process of file synchronization
 * Server process awaits and establishes the coonection with a
 * remote host, collects the files in its folder, send file names 
 * to the client host, send files that client host. 
 */

import java.io.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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
	
	public Vector<File> getFileList() {
		return fileList;
	}
	
	public Vector<String> getFileNameList() {
		return fileNameList;
	}
	
	public Vector<String> getMissingFileNameList(Vector<String> remoteFileNameList) {
		Vector<String> requestedFileNameList = new Vector<String>();
		requestedFileNameList.addAll(fileNameList);
		requestedFileNameList.removeAll(remoteFileNameList);
		return requestedFileNameList;
	}
	
	public Vector<File> prepareRequestedFile(Vector<String> requestedFileName) {
		Vector<File> preparedFile = new Vector<File>();
		for (File f: fileList) 
			if (requestedFileName.contains(f.getName()))
				preparedFile.add(f);
		return preparedFile;
	}
	
	public boolean writeFilesToDisk(Vector<File> files) {
		
		return true;
	}
	
	public String getPath() {
		return path.toString();
	}
}
