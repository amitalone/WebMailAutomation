package com.alacance.webMailAutomation.dblogging;

import java.util.ArrayList;
import java.util.List;

import com.alacance.webMailAutomation.UserAccount;

public class ActivityLog {
	private UserAccount userAccount;
	private int status;
	private String emailProvider;
	private int spamCount=0;
	private int mailReadCount=0;
	private long processingTime =0;
	private String failMessage;
	private String proxyUsed;
	private String activityServer;
	
	private List<ActivityDetailLog> details = new ArrayList<ActivityDetailLog>();
	
	public String getProxyUsed() {
		return proxyUsed;
	}
	public void setProxyUsed(String proxyUsed) {
		this.proxyUsed = proxyUsed;
	}
	public String getActivityServer() {
		return activityServer;
	}
	public void setActivityServer(String activityServer) {
		this.activityServer = activityServer;
	}
	public String getFailMessage() {
		return failMessage;
	}
	public void setFailMessage(String failMessage) {
		this.failMessage = failMessage;
	}
	public UserAccount getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getEmailProvider() {
		return emailProvider;
	}
	public void setEmailProvider(String emailProvider) {
		this.emailProvider = emailProvider;
	}
	public int getSpamCount() {
		return spamCount;
	}
	public void setSpamCount(int spamCount) {
		this.spamCount = spamCount;
	}
	public int getMailReadCount() {
		return mailReadCount;
	}
	public void setMailReadCount(int mailReadCount) {
		this.mailReadCount = mailReadCount;
	}
	public long getProcessingTime() {
		return processingTime;
	}
	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}
	public List<ActivityDetailLog> getDetails() {
		return details;
	}
	public void setDetails(List<ActivityDetailLog> details) {
		this.details = details;
	}
	public void addDetail(ActivityDetailLog detail) {
		this.details.add(detail);
	}
	
}
