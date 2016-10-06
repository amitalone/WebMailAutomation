package com.alacance.webMailAutomation.dblogging;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBLogThreadManager {
	private ExecutorService _executorService;
	private static DBLogThreadManager _instance = null;
	
	private DBLogThreadManager() {
		_executorService = Executors.newCachedThreadPool();
	}
	
	public static DBLogThreadManager getInstance() {
		if(null == _instance) {
			_instance = new DBLogThreadManager();
		}
		return _instance;
	}
	
	public void submitJob(ActivityLog activityLog) {
		_executorService.submit(new DBLoggerThread(activityLog));
	}
}
