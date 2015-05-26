package com.business;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.transferObject.FileTransferPropertyTO;


/**The sole responsibility of this utility class is to download file over SFTP
 * @author prasenjit.b
 */
public class SFTPUtilityMod {
	/**
	 * Related constants 
	 */
	static final String PROTOCOL = "sftp";
	static final String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
	static final String STRICT_HOST_KEY_CHECKING_VALUE = "no";
	static final int PORT = 22;
	
	/**This method is designed to download files from a remote location to local over SFTP
	 * @param fileTransferPropertyTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param filePattern A String Object representing file name patter (example file1_*.dat)
	 * @return A boolean value having variance true or false on success
	 */
	public boolean downloadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String filePattern, int retryCount) {
		Channel channel = null;
		boolean success = false;
		if(StringUtils.isNotBlank(filePattern) && fileTransferPropertyTO != null && fileTransferPropertyTO.validateFileTransferTO()){
			try{
				/**
				 * Get session using provided credentials
				 */
				Session session = getSession(fileTransferPropertyTO);
				/**
				 * Get SFTP channel using session object
				 */
				channel = getSFTPChannel(session);
				if(channel != null){
					/**
					 * Verify whether channel is connected or not
					 * 1. if it is not connected show error
					 * 2. if it is connected get the files from remote location to local 
					 */
					if(!channel.isConnected()){
						System.out.println("Could not connect to HOST ::"+fileTransferPropertyTO.toString());
					}else{
						try{
							ChannelSftp channelSftp = (ChannelSftp)channel;
							channelSftp.cd(fileTransferPropertyTO.getRemoteDirectory());
							Vector<ChannelSftp.LsEntry> lsEntryList = channelSftp.ls(filePattern);
							for(ChannelSftp.LsEntry entry : lsEntryList){
								if(entry != null && !entry.getAttrs().isDir()){
									String remoteFileName = entry.getFilename();
									if(StringUtils.isNotBlank(remoteFileName)){
										/*System.out.println("Downloading Remote file name ::"+remoteFileName);
										channelSftp.get(remoteFileName, fileTransferPropertyTO.getLocalDirectory()+"/"+remoteFileName);*/
										retryDownload(fileTransferPropertyTO, remoteFileName, channelSftp, retryCount);
									}
								}
							}
							success = true;
						}catch(SftpException sftpEx){
							System.out.println("Problem while getting file from SFTP");
							throw sftpEx;
						}finally{
							releaseResource(channel);
							releaseResource(session);
						}
					}
				}else{
					System.out.println("Channel not created, coming as NULL");
				}
			}catch(Exception ex){
				System.out.println("Problem while getting file from SFTP");
				ex.printStackTrace();
			}
		}
		return success;
	}  
	
	/**This method is designed to download remote file to local. If there is any exception while downloading file then
	 * this method will try to retry to download the same for a specific retry count. 
	 * @param fileTransferPropertyTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param remoteFileName A String representing the remote server file name
	 * @param channelSftp jsch.ChannelSftp Object
	 * @param retryCount A <code>int</code> value representing the retry count 
	 * @throws SftpException
	 */
	private void retryDownload(FileTransferPropertyTO fileTransferPropertyTO, String remoteFileName, ChannelSftp channelSftp, int retryCount) throws SftpException{
		int count = 1;
		boolean isDownloaded = false;
		/**
		 * Retry to download file from remote for provided retry count
		 */
		while(count <= retryCount){
			try{
				if(count == 1){
					System.out.println("Downloading Remote file name ::"+remoteFileName);
				}else{
					System.out.println("Trying to download Remote file name ::"+remoteFileName+" for times :: "+count);
				}
				channelSftp.get(remoteFileName, fileTransferPropertyTO.getLocalDirectory()+"/"+remoteFileName);
				isDownloaded = true;
				break;
			}catch(SftpException sftpException){
				count ++;
			}
		}
		/**
		 * if there is exception while downloading the file after trying for a specific retry count 
		 * then throw <code>SftpException</code>
		 */
		if(!isDownloaded){
			throw new SftpException(1, "Failed to download file :: "+remoteFileName);
		}
	}
	
	/**This method is designed to create jsch.Session Object using details as userid, serverName, port etc 
	 * @param fileTransferPropertyTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @return jsch.Session Object
	 * @throws Exception
	 */
	private Session getSession(FileTransferPropertyTO fileTransferPropertyTO) throws Exception{
		Session session = null;
		if(fileTransferPropertyTO != null && fileTransferPropertyTO.validateFileTransferTO()){
			try{
				JSch jsch = new JSch();
				session = jsch.getSession(fileTransferPropertyTO.getUserId(), fileTransferPropertyTO.getServerAddress(), PORT);
				session.setPassword(fileTransferPropertyTO.getPassword());
				java.util.Properties config = new java.util.Properties();
				config.put(STRICT_HOST_KEY_CHECKING, STRICT_HOST_KEY_CHECKING_VALUE);
				session.setConfig(config); 
				session.connect();
			}catch(JSchException jsex){
				System.out.println("Problem while creating SFTP session");
				throw jsex;
			}
		}
		return session;
	}
	
	/**This method is designed to create jsch.Channel Object using provided jsch.Session Object
	 * @param session jsch.Session Object
	 * @return jsch.Channel Object
	 * @throws Exception
	 */
	private Channel getSFTPChannel(Session session) throws Exception{
		Channel channel = null;
		if(session != null){
			try{
				channel = session.openChannel(PROTOCOL); 
				channel.connect();
			}catch(JSchException jsex){
				System.out.println("Problem while creating SFTP channel");
				throw jsex;
			}
		}else{
			System.out.println("Unable to create Channel as Session Object coming is NULL");
		}
		return channel;
	}
	
	/**This method's sole responsibility is to release Session Object
	 * @param session jsch.Session Object
	 */
	public void releaseResource(Session session){
		if(session != null){
			session.disconnect();
		}
	}
	
	/**This method's sole responsibility is to release Channel Object
	 * @param channel jsch.Channel Object
	 */
	public void releaseResource(Channel channel){
		if(channel != null){
			channel.disconnect();
		}
	}
	
	public static void main(String[] args) {
		SFTPUtility sftpUtility = new SFTPUtility();
		int retryCount = 1;
		String filePattern = "file_*.dat";
		FileTransferPropertyTO fileTransferPropertyTO = new FileTransferPropertyTO();
		fileTransferPropertyTO.setLocalDirectory("localDirectory");
		fileTransferPropertyTO.setRemoteDirectory("remoteDirectory");
		fileTransferPropertyTO.setUserId("userId");
		fileTransferPropertyTO.setPassword("password");
		fileTransferPropertyTO.setServerAddress("serverAddress");
		boolean status = sftpUtility.downloadFileUsingSFTP(fileTransferPropertyTO, filePattern, retryCount);
		System.out.println("downloadFileUsingSFTP status :: "+status);
	}
}
