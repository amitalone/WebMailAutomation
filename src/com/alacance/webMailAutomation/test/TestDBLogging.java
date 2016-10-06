package com.alacance.webMailAutomation.test;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.dao.LogDAO;
import com.alacance.webMailAutomation.dblogging.ActivityDetailLog;
import com.alacance.webMailAutomation.dblogging.ActivityLog;

public class TestDBLogging {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogDAO dao = LogDAO.getInstance();
		ActivityLog activityLog = new ActivityLog();
		
		ActivityDetailLog detail = new ActivityDetailLog();
		detail.setFrom("amit");
		detail.setLink("http://google.com");
		detail.setProcessingTime(10);
		detail.setSenderDomain("bulk.com");
		detail.setSubject("Hey Mister");
		activityLog.addDetail(detail);
		
		detail = new ActivityDetailLog();
		detail.setFrom("aroma");
		detail.setLink("http://sddsgle.com");
		detail.setProcessingTime(12);
		detail.setSenderDomain("Siniste.com");
		detail.setSubject("CocaCooola");
		activityLog.addDetail(detail);
		
		activityLog.setEmailProvider("goomail");
		activityLog.setMailReadCount(5);
		activityLog.setProcessingTime(100);
		activityLog.setSpamCount(2);
		activityLog.setStatus(1);
		
		UserAccount ua = new UserAccount();
		ua.setPassword("mypass");
		ua.setUserName("hellouser");
		activityLog.setUserAccount(ua);
		
		dao.logActivity(activityLog);
		
		activityLog = new ActivityLog();
		activityLog.setStatus(0);
		activityLog.setProcessingTime(50);
		ua = new UserAccount();
		ua.setPassword("myfailpass");
		ua.setUserName("baduser");
		activityLog.setUserAccount(ua);
		
		dao.logActivity(activityLog); 
		LogDAO.getInstance().updateJobStatus();
	}

}
