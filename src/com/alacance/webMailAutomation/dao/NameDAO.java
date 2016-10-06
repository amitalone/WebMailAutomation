package com.alacance.webMailAutomation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.alacance.webMailAutomation.NameDVO;
import com.alacance.webMailAutomation.ProxySettings;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class NameDAO {

	 private static NameDAO _instance = null;
	 private Connection connect = null;
	 private PreparedStatement statment;
	 Logger log = Logger.getLogger(NameDAO.class);
	 
	 private NameDAO() {
			try{
				 Class.forName("com.mysql.jdbc.Driver");
			     connect = DriverManager.getConnection(ResourceLoader.getMYSQLConnectionString(), ResourceLoader.getDBUser(), ResourceLoader.getDBPassword());
			     log.debug("LOG DAO Created.");
			}catch(Exception ex) {
				log.debug("ERROR Creating connection", ex);
			} 
	 }
	 
	 public static NameDAO getInstance() {
		 if(null == _instance) {
			 _instance = new NameDAO();
		 }
		 return _instance;
	 }
	 
	 public NameDVO getRandomName() {
		 NameDVO name = null;
		 try {
			 statment = connect.prepareStatement("select * from (select name first, gender from name_database_table_nadt where type = 'F' order by rand() limit 1) t1,(select name last from name_database_table_nadt where type = 'N' order by rand() limit 1) t2;");
			ResultSet rs =  statment.executeQuery();
			if (rs.next()) {
				name = new NameDVO();
				name.setFirst(rs.getString("first"));
				name.setLast(rs.getString("last"));
				name.setGender(rs.getString("gender"));
			}
			
		 }catch(Exception ex) {
				log.debug("Error ececuting statments", ex);
		 }	
		 return name;
	 }
}
