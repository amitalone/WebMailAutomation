package com.alacance.webMailAutomation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.alacance.webMailAutomation.dao.LogDAO;
import com.alacance.webMailAutomation.dao.ProxyDataDAO;
import com.alacance.webMailAutomation.tasks.AOLAccountCreatorTask;
import com.alacance.webMailAutomation.tasks.AOLReaderTask;
import com.alacance.webMailAutomation.tasks.GmailTaskThread;
import com.alacance.webMailAutomation.tasks.HotmailHeadlessReader;
import com.alacance.webMailAutomation.tasks.HotmailTaskThread;
import com.alacance.webMailAutomation.util.AccountIdentifire;
import com.alacance.webMailAutomation.util.ResourceLoader;
import com.sun.jna.platform.win32.Advapi32Util.Account;



public class TaskManager {
	
	private static TaskManager _instance = null;
	private ExecutorService _executorService;
	private List<Future<Integer>> _futureList = new ArrayList<Future<Integer>>();
	private final Logger log = Logger.getLogger(TaskManager.class);
	
	public static TaskManager getInstance() {
		if(null == _instance) {
			_instance = new TaskManager();
		}
		return _instance;
	}
	
	private TaskManager() {
		log.debug("Task manager instance created with default pool size " +  10);
		_executorService = Executors.newFixedThreadPool(10);
	}
	
	public void setThreadPoolSize(int size) {
		log.debug("Task manager instance updated with pool size " +  size);
		_executorService = Executors.newFixedThreadPool(size);
	}
	
	public void shutdown() {
		_executorService.shutdown();
	}
	
	public void submitJob(UserAccount userAccount, String isp) {
		log.debug("Submitting job to task manager");
		if("gmail".equalsIgnoreCase(isp)) {
			Future<Integer> future = _executorService.submit(new GmailTaskThread(userAccount));
			_futureList.add(future);
		}
		if("hotmail".equalsIgnoreCase(isp)) {
			Future<Integer> future = _executorService.submit(new HotmailTaskThread(userAccount));
			_futureList.add(future);
		}
		if("aol".equalsIgnoreCase(isp)) {
			Future<Integer> future = _executorService.submit(new AOLReaderTask(userAccount));
			_futureList.add(future);
		}
	}
	
	public void submitJob(List<UserAccount> userAccounts, String isp) {
		for(UserAccount account : userAccounts) {
			submitJob(account, isp);
		}
	//	LogDAO.getInstance().logStatus(userAccounts.size(), ResourceLoader.getServerName());
		waitForFuture();
		this.shutdown();
	}
	
	public void submitAnyDomainJob(List<UserAccount> userAccounts) {
		
		for(UserAccount account : userAccounts) {

			String type = AccountIdentifire.getType(account);
			if(type.equalsIgnoreCase("hotmail")) {
				Future<Integer> future = _executorService.submit(new HotmailHeadlessReader(account));
				_futureList.add(future);
			}
			if(type.equalsIgnoreCase("aol")) {
				//Future<Integer> future = _executorService.submit(new AOLReaderTask(userAccount));
				//_futureList.add(future);
			}
		}
		
		waitForFuture();
		this.shutdown();
	}
	
	public void submitCreateJob(String isp, int threadCount) {
		ProxyDataDAO dao = ProxyDataDAO.getInsatnce();
		for(int i=0; i< threadCount; i++) {
			if("aol".equalsIgnoreCase(isp)){
				Future<Integer> future = _executorService.submit(new AOLAccountCreatorTask(dao.getUnusedProxy()));
				_futureList.add(future);
			}
		}
		
		waitForFuture();
		this.shutdown();
	}
	
	
	private void waitForFuture() {
		log.info("Task Manager waiting for future");
		for (Future<Integer> future : _futureList) {
			try {
				future.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		log.info("Future retrived for all tasks");
	}
}
