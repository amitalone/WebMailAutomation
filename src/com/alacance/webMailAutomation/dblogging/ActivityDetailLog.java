package com.alacance.webMailAutomation.dblogging;

public class ActivityDetailLog {
	 private String subject;
	 private String from;
	 private String senderDomain;
	 private String link;
	 private long processingTime;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getSenderDomain() {
		return senderDomain;
	}
	public void setSenderDomain(String senderDomain) {
		this.senderDomain = senderDomain;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public long getProcessingTime() {
		return processingTime;
	}
	public void setProcessingTime(long processingTime) {
		this.processingTime = processingTime;
	}
	 
	 
}
