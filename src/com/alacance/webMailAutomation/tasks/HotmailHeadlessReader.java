package com.alacance.webMailAutomation.tasks;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.dao.LogDAO;
import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.dblogging.DBLogThreadManager;
import com.alacance.webMailAutomation.util.ResourceLoader;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;

public class HotmailHeadlessReader implements Callable<Integer>{

	WebDriver driver;
	private final Logger log = Logger.getLogger(HotmailTaskThread.class);
	private final UserAccount userAccount;
	private ActivityLog _actActivityLog = null;
	long startTime = 0;
	int conditionTimeOut = 60;
	private static  String mailIdentifire;
	private static  String clickLinkIdentifire;
	private int spamCount =0;
	private int readCount =0;
	private int emptyInboxWait = 60;
	private int emptyJunkWait = 60;
	private boolean forceClean = false;
	private int numberOfPagesToRead =0;
	
	public HotmailHeadlessReader(UserAccount userAccount) {
		this.userAccount = userAccount;
		
	}
	
	@Override
	public Integer call() throws Exception {
		initDrivers();
		try {
			if(login()) {
				checkWelcome();
				_actActivityLog.setStatus(1);
			    log.debug(userAccount.getUserName() + " Logged in");
				boolean emptyAndProcess = ResourceLoader.processMails();
				boolean emptyJunk = ResourceLoader.emptyJunk();
				boolean emptyInbox = ResourceLoader.emptyInbox();
				
				if(emptyAndProcess == true) {
					if(isSpamProcessingRequired()) {
						if(gotoJunk()) {
							processJunkMails();
						}
					}
					
					if(isInboxProccesingRequired()) {
						//gotoInbox();
						//forceClean = forceClean();
						//processInbox();
						
						for(int counter=1; counter<=numberOfPagesToRead;counter++) {
							// gotoNextPage();
							if(isInboxProccesingRequired()) {
								//processInbox();
							} 
						}
					}
					 
				}
				
				if(emptyJunk) {
					//emptyJunk();
				}
				if(emptyInbox) {
					//emptyInbox();
				}
				_actActivityLog.setMailReadCount(readCount);
				_actActivityLog.setSpamCount(spamCount);

			}else {
			    _actActivityLog.setStatus(0);
			    log.debug(userAccount.getUserName() + " Logged FAILED");
			}
			
		}catch(Exception ex) {
			log.debug("Exception in call ", ex);
		}finally {
			log.info("Window Closed");
			if(ResourceLoader.closeDriver()) {
				//driver.close();
				//forceCloseAllWindows();
				driver.quit();

			}
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    _actActivityLog.setProcessingTime(elapsedTime);
		   
		    //TODO:DB Logging
		      DBLogThreadManager.getInstance().submitJob(_actActivityLog);
			 LogDAO.getInstance().updateJobStatus();
		}
		return 1;
	}
	
private void initDrivers() {
		
		try {
			numberOfPagesToRead = Integer.parseInt(ResourceLoader.getConfigValue("hotmail.readpages"));
		}catch(Exception e) {
			numberOfPagesToRead = 0;
			e.printStackTrace();
		}
		mailIdentifire = ResourceLoader.getConfigValue("hotmail.mailIdentifire");
		clickLinkIdentifire = ResourceLoader.getConfigValue("hotmail.clickLinkIdentifire");
		if(null == userAccount.getProxySettings()) {
			driver = new HtmlUnitDriver();
			log.debug("Creating FX driver with no proxy");
		}else {
			 
			HtmlUnitDriver hud = new HtmlUnitDriver();
			hud.setJavascriptEnabled(true);
			//hud.setHTTPProxy(userAccount.getProxySettings().getHost(), Integer.parseInt(userAccount.getProxySettings().getPort()), null);
			driver = hud;
			log.debug("Creating FX driver with proxy");
		}
		
		
		_actActivityLog = new ActivityLog();
		_actActivityLog.setEmailProvider("hotmail");
		_actActivityLog.setUserAccount(userAccount);
		
		
		try {
			conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("hotmail.conditiontimeout"));
		}catch(Exception ex) {
			try {
				conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("conditiontimeout"));
			}catch(NumberFormatException nfe) {
				conditionTimeOut = 60;
			}
		}
		
		try {
			emptyInboxWait = Integer.parseInt(ResourceLoader.getConfigValue("hotmail.emptyInboxTime"));
		}catch(Exception ex) {
			emptyInboxWait = 30;
		}
		try {
			emptyJunkWait = Integer.parseInt(ResourceLoader.getConfigValue("hotmail.emptyJunkTime"));
		}catch(Exception ex) {
			emptyJunkWait = 30;
		}
		log.info("HotmailTaskThread instance created " + userAccount + " Proxy " + userAccount.getProxySettings());
	}

	private boolean login() {
		log.debug("Entreing login");
		driver.get("http://outlook.com");
	//	System.out.println(driver.getPageSource());
		WebElement query = driver.findElement(By.name("login"));
		query.sendKeys(userAccount.getUserName());
		query = driver.findElement(By.name("passwd"));
		query.sendKeys(userAccount.getPassword());
		query = driver.findElement(By.name("SI"));
		query.click();
		try {
			//delay(ExpectedConditions.titleContains(userAccount.getUserName()));
			try {
				WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000001"));
				log.info("Login successfull for " +userAccount.getUserName());
				return true;
			}catch(ElementNotFoundException enfe) {
				
			}
			
		}catch(TimeoutException te) {
			log.info("Time out occured in login process. Possible login failure.");
			String title = driver.getTitle();
			log.info("Timedout title " + driver.getTitle()); 
			if(title.contains("Call us overprotective")) {
			//	checkEmailVerification();
				delay(ExpectedConditions.titleContains(userAccount.getUserName()));
				return true;
			}
			
			if(title.contains("Outlook")) {
				return true;
			}
			if(title.contains("Sign In")) {
				try {
					driver.findElement(By.id("idTd_Tile_ErrorMsg_Login"));
					_actActivityLog.setFailMessage("Credentials failed.");
					log.info("Closing thread. Reason: Credentials failed for " +userAccount.getUserName());
					return false;

				}catch(NoSuchElementException nse) {
				}
				
				log.info("Closing thread. Reason: Unknown Exception " +userAccount.getUserName());
				return false;
				
			}
		}
		
		log.debug("Leaving login");
		return false;
	}
	
	private void checkWelcome() {
		log.info("Check welcome starts");
		try {
			
			List<WebElement> buttons = driver.findElements(By.tagName("button"));
			for(WebElement button : buttons) {
				if(button.getText().contains("Continue") || button.getText().contains("continue")) {
					button.click();
					try {
						Thread.sleep(1000 * 2);
					}catch(Exception ex) {
						
					}
				}
			}
			
			WebElement ele = driver.findElement(By.id("crm_bubble"));
			if(null!= ele) {
				ele = ele.findElement(By.id("default"));
				if(null != ele) {
					ele.click();
				}
			}
		}catch(NoSuchElementException nse) {
			
		}catch(Exception ex) {
			
		}
		log.info("Check welcome ends");
	}

	private boolean isInboxProccesingRequired() {
		log.debug("isInboxProccesingRequired start");
		WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000001"));
		//WebElement  element  = driver.findElement (By.partialLinkText("Inbox"));
		log.debug("isInboxProccesingRequired ends.");
		return element.getText().matches(".*\\d.*");
	}
	
	private void delay(ExpectedCondition<?> condition) {
			
			WebDriverWait wait = new WebDriverWait(driver, conditionTimeOut);
			log.info("wating for " + condition.toString());
			wait.until(condition);
			log.info("Done wating for "  + condition.toString());
	}
	
	private boolean isSpamProcessingRequired() {
		 log.debug("isSpamProcessingRequired start");
		 WebElement  element;
		try {
			 // Looking for SPAM link lable in page
			 element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000005"));
			 log.debug("isSpamProcessingRequired ends.");
			 return element.getText().matches(".*\\d.*");
		}catch(NoSuchElementException nse) {
			 
			// Forcefully load SPAM folder
			String junk = driver.getCurrentUrl();
			junk = junk + "&fid=5";
			driver.get(junk);
			log.debug("isSpamProcessingRequired ends.");

			return true;
		}
	}
	
	private boolean gotoJunk() {
		log.debug("gotoJunk start");
		WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000005"));
		try {
			element.click();
			//delay(ExpectedConditions.visibilityOfElementLocated(By.id("messageListContentContainer")));
			try {
				log.info("Wating 5 seconds");
				Thread.sleep(1000 * 5);
				log.info("Wait over");
			}catch(Exception ex) {
			}
		}catch (TimeoutException te) {
			log.info("Unable to load Spam folder");
			log.debug("gotoJunk ends");
			String junk = driver.getCurrentUrl();
			junk = junk + "&fid=5";
			driver.get(junk);
			delay(ExpectedConditions.titleContains(userAccount.getUserName()));
			return true;
		}
		element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000005"));
		String cssClass = element.getAttribute("class");
		//System.out.println(cssClass);
		if(!cssClass.contains("ItemSelected")) {
			gotoJunk();
		}
		/*if(!driver.getCurrentUrl().contains("fid=5")) {
			gotoJunk();
		}*/
		log.debug("gotoJunk ends");
		return true;
	}
	
	private  void processJunkMails() {
		log.debug("processJunkMails start");
		try {
			WebElement  element  = driver.findElement(By.id("messageListContentContainer"));
			List<WebElement> elements = element.findElements(By.tagName("li"));
			
			//Collections.shuffle(elements);
			for(WebElement emailRow : elements) {
				if(emailRow.findElement(By.tagName("a")).getAttribute("class").contains("TextSemiBold")) {
					String email = emailRow.findElement(By.xpath("//span[@email]")).getAttribute("email");
					if(isMailIdentifire(email)) {
						//WebElement chkBox = emailRow.findElement(By.xpath("//input[@type='checkbox']"));
						

						WebElement span = emailRow.findElement(By.name("mCC"));
						WebElement chkBox = emailRow.findElement(By.tagName("input"));
						chkBox.click();
						
						//delay(ExpectedConditions.visibilityOfElementLocated(By.id("MarkAsNotJunk")));
						delay2();
						driver.findElement(By.id("MarkAsNotJunk")).click();
						spamCount++;
					}
				}
 			}
			
		}catch(NoSuchElementException nse) {
		}
		
		log.debug("processJunkMails ends");
	}
	
	private boolean isMailIdentifire(String lable) {
		if(mailIdentifire.contains("*")) {
			return true;
		}
		return lable.contains(mailIdentifire);
	}
	
	private void delay2() {
		try {
			log.info("Wating 5 seconds");
			Thread.sleep(1000 * 2);
			log.info("Wait over");

		}catch(Exception ex){}
	}
}
