package com.alacance.webMailAutomation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.dblogging.SeedAccountDVO;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class SeedAccountDAO {

	 private static SeedAccountDAO _instance = null;
	 private Connection connect = null;
	 private PreparedStatement statment;
	 Logger log = Logger.getLogger(SeedAccountDAO.class);
	 
	 private SeedAccountDAO() {
			try{
				 Class.forName("com.mysql.jdbc.Driver");
			     connect = DriverManager.getConnection(ResourceLoader.getMYSQLConnectionString(), ResourceLoader.getDBUser(), ResourceLoader.getDBPassword());
			     log.debug("SeedAccountDAO Created.");
			}catch(Exception ex) {
				log.debug("ERROR Creating connection", ex);
			} 
	 }
	 
	 public static SeedAccountDAO getInstance() {
		 if(null == _instance) {
			 _instance = new SeedAccountDAO();
		 }
		 return _instance;
	 }
	 
	 public void updateAccountStatus(int id) {
			log.debug("updateAccountStatus starts");
			try {
				statment = connect.prepareStatement("update home_seed_data_table set status =1 where  hsdtid = ?;");
				statment.setInt(1, id);
				statment.executeUpdate();
			}catch(Exception ex) {
				log.debug("Error ececuting statments", ex);
			}
			log.debug("updateAccountStatus ends");
	 }
	 public int saveAccount(SeedAccountDVO seedAccount)  {
			log.debug("saveAccount starts");
			int id=0;
			try {
				statment = connect.prepareStatement("INSERT INTO reports.home_seed_data_table" +
						"(fname, lname, username, `password`, proxy, bmonth, bday, byear, phone, currentemail, captcha, gender, location, proxyserver, zip, secQue, secAns, provider) " +
						"VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);", Statement.RETURN_GENERATED_KEYS);
				statment.setString(1, seedAccount.getFname());
				statment.setString(2, seedAccount.getLname());
				statment.setString(3, seedAccount.getUsername());
				statment.setString(4, seedAccount.getPassword());
				statment.setString(5, seedAccount.getProxy());
				statment.setString(6, seedAccount.getBmonth());
				statment.setInt(7, seedAccount.getBday());
				statment.setInt(8, seedAccount.getByear());
				statment.setString(9, seedAccount.getPhone());
				statment.setString(10, seedAccount.getCurrentemail());
				statment.setString(11, seedAccount.getCaptcha());
				statment.setString(12, seedAccount.getGender());
				statment.setString(13, seedAccount.getLocation());
				statment.setString(14, seedAccount.getProxyserver());
				statment.setString(15, seedAccount.getZip());
				statment.setString(16, seedAccount.getSecQuestion());
				statment.setString(17, seedAccount.getSecAnswer());
				statment.setString(18, seedAccount.getProvider());
				statment.executeUpdate();
				ResultSet rs =  statment.getGeneratedKeys();
				if (rs.next()) {
					  id = rs.getInt(1);
				}
			}catch(Exception ex) {
				log.debug("Error ececuting statments", ex);
			}
			log.debug("saveAccount ends");
			return id;
	 }
}
