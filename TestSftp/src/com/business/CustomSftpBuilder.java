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

	/**
	 * @param fileTransferTO
	 * @param fileToDownload
	 * @return
	 */
	public boolean downloadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToDownload){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToDownload)){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				validateFileTransferTO(fileTransferPropertyTO);
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
	
	/**
	 * @param fileTransferTO
	 * @param fileToUpload
	 * @return
	 */
	public boolean uploadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToUpload){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToUpload)){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				validateFileTransferTO(fileTransferPropertyTO);
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
	
	/**
	 * @param fileTransferTO
	 * @param fileToDelete
	 * @return
	 */
	public boolean deleteFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String fileToDelete){
		boolean success = false;
		if(StringUtils.isNotBlank(fileToDelete)){
			StandardFileSystemManager manager = new StandardFileSystemManager();
			try{
				validateFileTransferTO(fileTransferPropertyTO);
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
	
	/**
	 * @param opts
	 * @throws Exception
	 */
	private void setUpSFTP(FileSystemOptions opts) throws Exception{
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, true);
		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
	}
	
	/**
	 * @param fileTransferTO
	 * @throws Exception
	 */
	private void validateFileTransferTO(FileTransferPropertyTO fileTransferTO) throws Exception{
		if(fileTransferTO != null){
			if(StringUtils.isBlank(fileTransferTO.getServerAddress())){
				throw new Exception("ServerAddress is coming as Null. Please verify");
			}
			if(StringUtils.isBlank(fileTransferTO.getUserId())){
				throw new Exception("UserId is coming as Null. Please verify");
			}
			if(StringUtils.isBlank(fileTransferTO.getPassword())){
				throw new Exception("Password is coming as Null. Please verify");
			}
			if(StringUtils.isBlank(fileTransferTO.getLocalDirectory())){
				throw new Exception("LocalDirectory is coming as Null. Please verify");
			}
			if(StringUtils.isBlank(fileTransferTO.getRemoteDirectory())){
				throw new Exception("RemoteDirectory is coming as Null. Please verify");
			}
		}else{
			throw new Exception("fileTransferTO is coming as Null. Please verify");
		}
	}
}
