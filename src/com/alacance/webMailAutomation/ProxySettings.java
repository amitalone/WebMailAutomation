package com.alacance.webMailAutomation;

public class ProxySettings {
	public ProxySettings(String host, String port) {
		super();
		this.host = host;
		this.port = port;
	}

	private String host;
	private String port;
	private String serverTag;
	
	public String getServerTag() {
		return serverTag;
	}
	public void setServerTag(String serverTag) {
		this.serverTag = serverTag;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	
	public String toString() {
		return "ProxySettings [host=" + host + ", port=" + port + "]";
	}
	
}
