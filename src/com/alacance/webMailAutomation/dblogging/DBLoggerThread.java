package com.alacance.webMailAutomation.dblogging;

import com.alacance.webMailAutomation.dao.LogDAO;

public class DBLoggerThread implements Runnable{

	private ActivityLog activityLog;
	
	public DBLoggerThread(ActivityLog activityLog) {
		this.activityLog = activityLog;
	}
	
	public void run() {
		LogDAO.getInstance().logActivity(activityLog);
	}

}
