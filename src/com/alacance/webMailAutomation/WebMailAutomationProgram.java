package com.alacance.webMailAutomation;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.alacance.webMailAutomation.util.ResourceLoader;
import com.beust.jcommander.JCommander;

public class WebMailAutomationProgram {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			
			WebMailCommander commander = new WebMailCommander();
			JCommander jc = new JCommander(commander, args);
			
			if(null!= commander.help) {
				jc.usage();
				System.exit(0);
			}
			if(null == commander.isp) {
				System.out.println("ISP is mandetory");
				jc.usage();
			}
			
			if(!(commander.isp.equalsIgnoreCase("gmail") || commander.isp.equalsIgnoreCase("hotmail") || commander.isp.equalsIgnoreCase("aol"))) {
				System.out.println("Only hotmail & Gmail & aol ISP are implemented.");
				System.exit(0);
			}
			
			Properties props = new Properties();
			props.load(new FileInputStream("conf/log4j.properties"));
			PropertyConfigurator.configure(props);
			
			TaskManager taskManager = TaskManager.getInstance();
			int threadCount = ResourceLoader.getThreadPoolSize();
			taskManager.setThreadPoolSize(threadCount);
			Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.OFF);
			
			if(null == commander.mode || "read".equalsIgnoreCase(commander.mode)) {
				if(null != commander.accounts) {
					ResourceLoader.accountPath = commander.accounts;
				}
				
				ResourceLoader.initAccounts();
				ResourceLoader.initHMVerificationIds();
				
				
				taskManager.submitJob(ResourceLoader.getAccounts(), commander.isp);
			}
			
			if("create".equalsIgnoreCase(commander.mode)) {
				ResourceLoader.initDomains();
				ResourceLoader.initPasswords();
				ResourceLoader.initPhoneBook();
				ResourceLoader.initSchool();
				ResourceLoader.initSuffixes();
				ResourceLoader.iniZip();
				
				taskManager.submitCreateJob(commander.isp, threadCount);
			}
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Program Finished.");
		 
	}

}
