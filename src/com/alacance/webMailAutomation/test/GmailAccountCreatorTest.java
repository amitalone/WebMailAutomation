package com.alacance.webMailAutomation.test;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.alacance.webMailAutomation.dao.ProxyDataDAO;
import com.alacance.webMailAutomation.tasks.GmailAccountCreatorTask;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class GmailAccountCreatorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		 
		Properties props = new Properties();
		props.load(new FileInputStream("conf/log4j.properties"));
		PropertyConfigurator.configure(props);
		ResourceLoader.getAccounts();
		Logger.getLogger("org.apache.http").setLevel(org.apache.log4j.Level.OFF);
		ResourceLoader.initPhoneBook();
		ResourceLoader.initDomains();
		ResourceLoader.initSuffixes();
		ResourceLoader.initPasswords();
		 new GmailAccountCreatorTask(null).call(); 
		
		 
	}

}
