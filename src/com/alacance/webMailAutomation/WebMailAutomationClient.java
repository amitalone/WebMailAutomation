package com.alacance.webMailAutomation;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alacance.webMailAutomation.util.ResourceLoader;

public class WebMailAutomationClient {
	public static void main(String[] args) {
		try {
			String fileName = args[0];
			String option = args[1];
			System.out.println(fileName + " " + option);
			
			Properties props = new Properties();
			props.load(new FileInputStream("conf/log4j.properties"));
			PropertyConfigurator.configure(props);
			Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.OFF);
			Logger.getLogger("com.gargoylesoftware.htmlunit.javascript").setLevel(org.apache.log4j.Level.OFF);
			Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(org.apache.log4j.Level.OFF);
			
			
			ResourceLoader.accountPath = fileName;
			ResourceLoader.initAccounts();
			
			TaskManager manager = TaskManager.getInstance();
			manager.setThreadPoolSize(1);
			manager.submitAnyDomainJob(ResourceLoader.getAccounts());
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
