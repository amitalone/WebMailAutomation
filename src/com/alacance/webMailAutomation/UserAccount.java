package com.alacance.webMailAutomation;

public class UserAccount {
	private String userName;
	private String password;
	private ProxySettings proxySettings;
	private String recoveryEmail;
	private String recoveryPhone;
	 
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public ProxySettings getProxySettings() {
		return proxySettings;
	}
	public void setProxySettings(ProxySettings ps) {
		this.proxySettings = ps;
	}
	
	public void setProxySettings(String server, String port) {
		this.proxySettings = new ProxySettings(server, port);
	}
	public String getRecoveryEmail() {
		return recoveryEmail;
	}
	public void setRecoveryEmail(String recoveryEmail) {
		this.recoveryEmail = recoveryEmail;
	}
	public String getRecoveryPhone() {
		return recoveryPhone;
	}
	public void setRecoveryPhone(String recoveryPhone) {
		this.recoveryPhone = recoveryPhone;
	}
	@Override
	public String toString() {
		return "UserAccount [userName=" + userName + ", password=" + password
				+ ", proxySettings=" + proxySettings + ", recoveryEmail="
				+ recoveryEmail + ", recoveryPhone=" + recoveryPhone + "]";
	}
	
	
}
