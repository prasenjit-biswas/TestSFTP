package com.transferObject;

import org.apache.commons.lang3.StringUtils;

public class FileTransferPropertyTO {
	
	private String serverAddress = null;
	private String userId = null;
	private String password = null;
	private String remoteDirectory = null;
	private String 	localDirectory = null;
	
	public String getServerAddress() {
		return serverAddress;
	}
	public void setServerAddress(String serverAddress) {
		if(StringUtils.isNotBlank(serverAddress)){
			this.serverAddress = serverAddress.trim();
		}
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		if(StringUtils.isNotBlank(userId)){
			this.userId = userId.trim();
		}
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		if(StringUtils.isNotBlank(password)){
			this.password = password.trim();
		}
	}
	public String getRemoteDirectory() {
		return remoteDirectory;
	}
	public void setRemoteDirectory(String remoteDirectory) {
		if(StringUtils.isNotBlank(remoteDirectory)){
			this.remoteDirectory = remoteDirectory.trim();
		}
	}
	public String getLocalDirectory() {
		return localDirectory;
	}
	public void setLocalDirectory(String localDirectory) {
		if(StringUtils.isNotBlank(localDirectory)){
			this.localDirectory = localDirectory.trim();
		}
	}
	
	@Override
	public String toString() {
		return "FileTransferPropertyTO [serverAddress=" + serverAddress
				+ ", userId=" + userId + ", password=" + password
				+ ", remoteDirectory=" + remoteDirectory + ", localDirectory="
				+ localDirectory + "]";
	}
	
}
