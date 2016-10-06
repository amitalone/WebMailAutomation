package com.alacance.webMailAutomation.test;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.tasks.HotmailTaskThread;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class TestMailDrivers {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
	 	
		Properties props = new Properties();
		props.load(new FileInputStream("conf/log4j.properties"));
		PropertyConfigurator.configure(props);
		ResourceLoader.getAccounts();
		Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.OFF);
		
		UserAccount account = new UserAccount();
		account.setUserName("sachinnaik132@outlook.com");
		account.setPassword("Newuser123");
		
		HotmailTaskThread t = new HotmailTaskThread(account);
		t.call();
		
	}

}
