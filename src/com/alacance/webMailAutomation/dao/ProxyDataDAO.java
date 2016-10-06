package com.alacance.webMailAutomation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.alacance.webMailAutomation.ProxySettings;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class ProxyDataDAO {

	private static ProxyDataDAO _insatnce = null;
	private Connection connect = null;
	private PreparedStatement statment;
	Logger log = Logger.getLogger(ProxyDataDAO.class);
	 
	private ProxyDataDAO(){
		try{
			 Class.forName("com.mysql.jdbc.Driver");
		     connect = DriverManager.getConnection(ResourceLoader.getMYSQLConnectionString(), ResourceLoader.getDBUser(), ResourceLoader.getDBPassword());
		     log.debug("ProxyDataDAO Created.");
		}catch(Exception ex) {
			log.debug("ERROR Creating connection", ex);
		}
	}
	
	public static ProxyDataDAO getInsatnce() {
		if(null == _insatnce ) {
			_insatnce = new ProxyDataDAO();
		}
		return _insatnce;
	}
	

	public ProxySettings getUnusedProxy() {
		 ProxySettings proxySettings = null;
		 try {
			 statment = connect.prepareStatement("select proxy, port, servertag FROM proxy_server_data_table_psdt where used =0 order by rand();");
			 
			ResultSet rs = statment.executeQuery();
			if (rs.next()) {
				String prx = rs.getString("proxy");
				int port = rs.getInt("port");
				proxySettings  = new ProxySettings(prx, port+"");
				proxySettings.setServerTag(rs.getString("servertag"));
			}
			
		 }catch(Exception ex) {
				log.debug("Error ececuting statments", ex);
		 }	
		 return proxySettings;
	}
	
	public void updateProxyStatus(String proxyIP) {
		 try {
			 statment = connect.prepareStatement("update proxy_server_data_table_psdt set used = 1 where proxy = ?;");
			 statment.setString(1, proxyIP);
			 statment.executeUpdate();
			
		 }catch(Exception ex) {
				log.debug("Error ececuting statments", ex);
		 }
	}
}
