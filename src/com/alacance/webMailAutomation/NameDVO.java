package com.alacance.webMailAutomation;

public class NameDVO {
	@Override
	public String toString() {
		return "NameDVO [first=" + first + ", last=" + last + ", gender="
				+ gender + "]";
	}
	private String first;
	private String last;
	private String gender;
	private String email;
	
	
	public String getEmail() {
		return first+last;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirst() {
		return first;
	}
	public void setFirst(String first) {
		this.first = first;
	}
	public String getLast() {
		return last;
	}
	public void setLast(String last) {
		this.last = last;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	
	
	
}
