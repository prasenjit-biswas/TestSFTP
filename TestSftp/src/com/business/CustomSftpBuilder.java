package com.business;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.transferObject.FileTransferPropertyTO;


/**
 * @author USER
 *
 */
public class CustomSftpBuilder {

	/**This method is designed to download file from remote location to 
	 * local location using SFTP
	 * @param fileTransferTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param fileToDownload A String Object representing the name of the file which is required to download
	 * @return A boolean value having variance true or false on success
	 */
	public boolean downloadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToDownload){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToDownload) && fileTransferPropertyTO != null && fileTransferPropertyTO.validateFileTransferTO()){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				manager.init();
				FileSystemOptions opts = new FileSystemOptions();
				setUpSFTP(opts);
				//Create the SFTP URI using the host name, userid, password,  remote path and file name
				String sftpUri = new StringBuilder("sftp://").append(fileTransferPropertyTO.getUserId()).append(":").append(fileTransferPropertyTO.getPassword()).append("@")
						.append(fileTransferPropertyTO.getServerAddress()).append("/").append(fileTransferPropertyTO.getRemoteDirectory()).append(fileToDownload).toString();
				// Create local file object
				String filepath = new StringBuilder(fileTransferPropertyTO.getLocalDirectory()).append(fileToDownload).toString();
				File file = new File(filepath);
				FileObject localFile = manager.resolveFile(file.getAbsolutePath());
				// Create remote file object
				FileObject remoteFile = manager.resolveFile(sftpUri, opts);
				// Copy local file to sftp server
				localFile.copyFrom(remoteFile, Selectors.SELECT_SELF);
				System.out.println("File downloaded successfully");
				success = true;
			}catch(Exception ex){
				System.out.println("Problem while downloading file :: "+fileToDownload);
				ex.printStackTrace();
			}finally{
				manager.close();
			}
		}else{
			System.out.println("fileToDownload is coming as Null or Empty. Please verify");
		}
		return success;
	}
	
	/**This method is designed to upload a file from local location to 
	 * remote location using SFTP
	 * @param fileTransferTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param fileToDownload A String Object representing the name of the file which is required to upload
	 * @return A boolean value having variance true or false on success
	 */
	public boolean uploadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToUpload){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToUpload) && fileTransferPropertyTO != null && fileTransferPropertyTO.validateFileTransferTO()){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				//check if the file exists
				String filepath = new StringBuilder(fileTransferPropertyTO.getLocalDirectory()).append(fileToUpload).toString();
				File file = new File(filepath);
				if (!file.exists()){
					throw new RuntimeException("Error. Local file not found");
				}
				//Initializes the file manager
				manager.init();
				//Setup our SFTP configuration
				FileSystemOptions opts = new FileSystemOptions();
				setUpSFTP(opts);
				//Create the SFTP URI using the host name, userid, password,  remote path and file name
				String sftpUri = new StringBuilder("sftp://").append(fileTransferPropertyTO.getUserId()).append(":").append(fileTransferPropertyTO.getPassword()).append("@")
						.append(fileTransferPropertyTO.getServerAddress()).append("/").append(fileTransferPropertyTO.getRemoteDirectory()).append(fileToUpload).toString();
				// Create local file object
				FileObject localFile = manager.resolveFile(file.getAbsolutePath());
				// Create remote file object
				FileObject remoteFile = manager.resolveFile(sftpUri, opts);
				// Copy local file to sftp server
				remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
				System.out.println("File uploaded successfully");
				success = true;
			}catch(Exception ex){
				System.out.println("Problem while uploading file :: "+fileToUpload);
				ex.printStackTrace();
			}finally{
				manager.close();
			}
		}else{
			System.out.println("fileToUpload is coming as Null or Empty. Please verify");
		}
		return success;
	}
	
	/**This method is designed to delete a file from remote location 
	 * @param fileTransferTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param fileToDownload A String Object representing the name of the file which is required to delete
	 * @return A boolean value having variance true or false on success
	 */
	public boolean deleteFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToDelete){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToDelete) && fileTransferPropertyTO != null && fileTransferPropertyTO.validateFileTransferTO()){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				manager.init();
				//Setup our SFTP configuration
				FileSystemOptions opts = new FileSystemOptions();
				setUpSFTP(opts);
				//Create the SFTP URI using the host name, userid, password,  remote path and file name
				String sftpUri = new StringBuilder("sftp://").append(fileTransferPropertyTO.getUserId()).append(":").append(fileTransferPropertyTO.getPassword()).append("@")
						.append(fileTransferPropertyTO.getServerAddress()).append("/").append(fileTransferPropertyTO.getRemoteDirectory()).append(fileToDelete).toString();
				//Create remote file object
				FileObject remoteFile = manager.resolveFile(sftpUri, opts);
				//Check if the file exists
				if(remoteFile.exists()){
					remoteFile.delete();
					System.out.println("File deleted successful");
					success = true;
				}
			}catch(Exception ex){
				System.out.println("Problem while deleting file :: "+fileToDelete);
				ex.printStackTrace();
			}finally{
				manager.close();
			}
		}else{
			System.out.println("fileToDelete is coming as Null or Empty. Please verify");
		}
		return success;
	}
	
	/**Th
	 * @param opts
	 * @throws Exception
	 */
	private void setUpSFTP(FileSystemOptions opts) throws Exception{
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
	}
	
}
