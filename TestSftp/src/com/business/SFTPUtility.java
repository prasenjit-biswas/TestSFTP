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
public class SFTPUtility {
	
	/**
	 * Make this class single-tone
	 */
	private static SFTPUtility sftpUtility;
	
	private SFTPUtility(){}
	
	public static SFTPUtility getInstance(){
		if(sftpUtility == null){
			sftpUtility = new SFTPUtility();
		}
		return sftpUtility;
	}
	
	/**This method is designed to download files from a remote location to local over SFTP
	 * @param fileTransferPropertyTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @param filePattern A String Object representing file name patter (example file1_*.dat)
	 * @return A boolean value having variance true or false on success
	 */
	public boolean downloadFileUsingSFTP(FileTransferPropertyTO fileTransferPropertyTO, String filePattern) {
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
						System.out.println("Could not connect to HOST : "+fileTransferPropertyTO.toString());
					}else{
						try{
							ChannelSftp channelSftp = (ChannelSftp)channel;
							channelSftp.cd(fileTransferPropertyTO.getRemoteDirectory());
							Vector<ChannelSftp.LsEntry> lsEntryList = channelSftp.ls(filePattern);
							for(ChannelSftp.LsEntry entry : lsEntryList){
								if(!entry.getAttrs().isDir()){
									System.out.println("file name ::"+entry.getFilename());
									channelSftp.get(entry.getFilename(), fileTransferPropertyTO.getLocalDirectory()+"/"+entry.getFilename());
								}
							}
							success = true;
						}catch(SftpException sftpex){
							System.out.println("Problem while getting file from SFTP");
							throw sftpex;
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
	
	/**This method is designed to create jsch.Session Object using details as userid, serverName, port etc 
	 * @param fileTransferPropertyTO A custom object contains information like serverAddress, userId, password, remoteDirectory, localDirectory etc.
	 * @return jsch.Session Object
	 * @throws Exception
	 */
	private Session getSession(FileTransferPropertyTO fileTransferPropertyTO) throws Exception{
		Session session = null;
		if(fileTransferPropertyTO.validateFileTransferTO()){
			try{
				JSch jsch = new JSch();
				session = jsch.getSession(fileTransferPropertyTO.getUserId(), fileTransferPropertyTO.getServerAddress(), 22);
				session.setPassword(fileTransferPropertyTO.getPassword());
				java.util.Properties config = new java.util.Properties();
				config.put("StrictHostKeyChecking", "no");
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
				channel = session.openChannel("sftp"); 
				channel.connect();
			}catch(JSchException jsex){
				System.out.println("Problem while creating SFTP channel");
				throw jsex;
			}
		}else{
			System.out.println("Session Object is NULL");
		}
		return channel;
	}
	
	/**This method's sole responsibility is to release Session Object
	 * @param session
	 */
	public void releaseResource(Session session){
		if(session != null){
			session.disconnect();
		}
	}
	
	/**This method's sole responsibility is to release Channel Object
	 * @param session
	 */
	public void releaseResource(Channel channel){
		if(channel != null){
			channel.disconnect();
		}
	}
}
