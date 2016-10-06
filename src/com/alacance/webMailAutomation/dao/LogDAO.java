package com.alacance.webMailAutomation.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;

import com.alacance.webMailAutomation.dblogging.ActivityDetailLog;
import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class LogDAO {

	 private static LogDAO _instance = null;
	 private Connection connect = null;
	 private PreparedStatement statment;
	 Logger log = Logger.getLogger(LogDAO.class);
	 private static int statusId =0;
 
	 private LogDAO() {
		try{
			 Class.forName("com.mysql.jdbc.Driver");
		     connect = DriverManager.getConnection(ResourceLoader.getMYSQLConnectionString(), ResourceLoader.getDBUser(), ResourceLoader.getDBPassword());
		     log.debug("LOG DAO Created.");
		}catch(Exception ex) {
			log.debug("ERROR Creating connection", ex);
		}
	 }
	 
	 public static LogDAO getInstance() {
		 if(_instance == null) {
			 _instance = new LogDAO();
		 }
		 return _instance;
	 }
	 
	 
	 
	 
	 public void updateJobStatus() {
		 /*if(statusId == 0) {
			 try {
			statment = connect.prepareStatement("select wjst_id from wma_job_status_table_wjst where servername= ? order by wjst_id desc limit 1");
			statment.setString(1, ResourceLoader.getServerName());
			ResultSet rs =  statment.executeQuery();
			
			if (rs.next()) {
				statusId = rs.getInt(1);
			}
			
			}catch(Exception ex) {
					log.debug(statment);
					log.debug("Error ececuting statments", ex);
			} 
		 }
		 if(statusId > 0) {
			 try {
				 statment = connect.prepareStatement("UPDATE wma_job_status_table_wjst SET job_finished=job_finished+1 WHERE wjst_id=?");
				 statment.setInt(1, statusId);
				 statment.executeUpdate();
			 }catch(Exception ex) {
					log.debug(statment);
					log.debug("Error ececuting statments", ex);
			 }
		 }*/


	 }
	
	 
	 
	public void logActivity(ActivityLog activityLog)  {
		 int status = activityLog.getStatus();
		try {
			log.debug("logActivity starts");
			
			if(status == 0) {
				statment = connect.prepareStatement("INSERT INTO reports.wma_taskactivity_table " +
						"(username, pass, status, emailprovider, failMessage, processingTime, proxyused, proxyserver, audit_insert) VALUES (?, ?, ?, ?,?,?,?,?, current_timestamp());");
				if(null == activityLog.getUserAccount().getUserName()) {
					statment.setString(1, "NA");
					
				}else {
					statment.setString(1, activityLog.getUserAccount().getUserName());
					
				}
				if(null == activityLog.getUserAccount().getPassword()) {
					statment.setString(2, "NA");
					
				}else {
					statment.setString(2, activityLog.getUserAccount().getPassword());
				}
				statment.setInt(3, status);
				if(null == activityLog.getEmailProvider()) {
					statment.setString(4, "NA");
				}else {
					statment.setString(4, activityLog.getEmailProvider());
				}
				if(null == activityLog.getFailMessage()) {
					statment.setString(5, "NA");
				}else {
					statment.setString(5, activityLog.getFailMessage());
				}
				if(activityLog.getProcessingTime() == 0 || activityLog.getProcessingTime() == Double.NaN) {
					statment.setLong(6, 0);
				}else {
					statment.setLong(6, activityLog.getProcessingTime() / 1000);
				}

				if(null == activityLog.getProxyUsed()) {
					statment.setString(7, "NA");
				}else {
					statment.setString(7, activityLog.getProxyUsed());
				}
				
				if(null == activityLog.getActivityServer()) {
					statment.setString(8, "NA");
				}else {
					statment.setString(8, activityLog.getActivityServer());
				}
				
				statment.executeUpdate();
				log.debug("Activity added to DB with status 0");
			}
			if(status == 1) {
				int id =0;
				statment = connect.prepareStatement("INSERT INTO reports.wma_taskactivity_table " +
						"(username, pass, status, emailprovider, spamcount, mailreadcount, processingTime, failMessage, proxyused, proxyserver,audit_insert ) VALUES " +
						"(?, ?, ?, ?, ?, ?, ?, ?, ?,?, current_timestamp());", Statement.RETURN_GENERATED_KEYS);
				
				if(null == activityLog.getUserAccount().getUserName()) {
					statment.setString(1, "NA");
					
				}else {
					statment.setString(1, activityLog.getUserAccount().getUserName());
					
				}
				if(null == activityLog.getUserAccount().getPassword()) {
					statment.setString(2, "NA");
					
				}else {
					statment.setString(2, activityLog.getUserAccount().getPassword());
				}
				statment.setInt(3, status);
				if(null == activityLog.getEmailProvider()) {
					statment.setString(4, "NA");
				}else {
					statment.setString(4, activityLog.getEmailProvider());
				}
				if(activityLog.getSpamCount() == 0 || activityLog.getSpamCount() == Double.NaN) {
					statment.setInt(5, 0);

				}else {
					statment.setInt(5, activityLog.getSpamCount());

				}
				if(activityLog.getMailReadCount() == 0 || activityLog.getMailReadCount() == Double.NaN) {
					statment.setInt(6, 0);

				}else {
					statment.setInt(6, activityLog.getMailReadCount());

				}
				if(activityLog.getProcessingTime() == 0 || activityLog.getProcessingTime() == Double.NaN) {
					statment.setLong(7, 0);
				}else {
					statment.setLong(7, activityLog.getProcessingTime() / 1000);
				}
				
				if(null == activityLog.getFailMessage()) {
					statment.setNull(8, Types.VARCHAR);
				}else {
					statment.setString(8, activityLog.getFailMessage());
				}

				if(null == activityLog.getProxyUsed()) {
					statment.setString(9, "NA");
				}else {
					statment.setString(9, activityLog.getProxyUsed());
				}
				
				if(null == activityLog.getActivityServer()) {
					statment.setString(10, "NA");
				}else {
					statment.setString(10, activityLog.getActivityServer());
				}
				
				statment.executeUpdate();
				ResultSet rs =  statment.getGeneratedKeys();
				if (rs.next()) {
					  id = rs.getInt(1);
				}
				log.debug("Activity added to DB with status 1");
				if(id > 0) {
					List<ActivityDetailLog> details = activityLog.getDetails();
					for(ActivityDetailLog detail : details) {
						statment = connect.prepareStatement("INSERT INTO reports.wma_taskactivity_detail_table " +
								"(wtat_id, subject, `from`, senderdomain, link, processingTime)  VALUES " +
								"(?, ?, ?, ?, ?, ?);");
						statment.setInt(1, id);
						
						if(null == detail.getSubject()) {
							statment.setString(2, "NA");

						}else {
							statment.setString(2, detail.getSubject());
						}
						if(null == detail.getFrom()) {
							statment.setString(3, "NA");
						}else {
							if(detail.getFrom().length() > 254) {
								detail.setFrom(detail.getFrom().substring(0, 254));
							}
							statment.setString(3, detail.getFrom());
						}
						if(null == detail.getSenderDomain()) {
							statment.setString(4, "NA");
						}else {
							if(detail.getSenderDomain().length() > 254) {
								detail.setSenderDomain(detail.getSenderDomain().substring(0, 254));
							}
							statment.setString(4, detail.getSenderDomain());
						}
						if(null == detail.getLink()) {
							statment.setString(5, "NA");

						}else {
							statment.setString(5, detail.getLink());
						}
						if(detail.getProcessingTime() == 0 || detail.getProcessingTime() == Double.NaN) {
							statment.setLong(6, 0);
						}else {
							statment.setLong(6, detail.getProcessingTime() / 1000);
						}
						
						statment.executeUpdate();
						log.debug("Activity detail added to DB");
					}
				}
			}
		}catch(Exception ex) {
			log.debug("Error ececuting statments", ex);
		}
		log.debug("logActivity ends"); 
	}

}
