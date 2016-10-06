package com.alacance.webMailAutomation.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.dblogging.ActivityDetailLog;
import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.util.RangeRandom;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class AOLReaderTask implements Callable<Integer>{
	WebDriver driver;
	private final Logger log = Logger.getLogger(AOLReaderTask.class);
	private final UserAccount userAccount;
	//private static  String mailIdentifire;
	private static  String clickLinkIdentifire;
	private int spamCount =0;
	private int readCount =0;
	private ActivityLog _actActivityLog = null;
	long startTime = 0;
	int conditionTimeOut = 60;
	private List<String> _multiMailIdentifire;
	 
	public AOLReaderTask(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
	
	private void initDrivers() {
		startTime = System.currentTimeMillis();
		
		_multiMailIdentifire = ResourceLoader.getAOLMultiMailIdentifire();
		clickLinkIdentifire = ResourceLoader.getConfigValue("aol.clickLinkIdentifire");
		String proxyServer ="";
		 
		
		if(null == userAccount.getProxySettings()) {
			driver = new FirefoxDriver();
			log.debug("Creating FX driver with no proxy");
		}else {
			FirefoxProfile profile = new FirefoxProfile();
			Proxy proxy = new Proxy();
			proxyServer = userAccount.getProxySettings().getHost() + ":"+userAccount.getProxySettings().getPort();
			proxy.setHttpProxy(proxyServer);
			proxy.setFtpProxy(proxyServer);
			proxy.setSslProxy(proxyServer);
		//	profile.setProxyPreferences(proxy);
			
			profile.setPreference("dom.max_script_run_time", 3600000);
	        profile.setPreference("webdriver.load.strategy", "fast");
	        
			driver = new FirefoxDriver(profile);
			log.debug("Creating FX driver with proxy " + userAccount.getProxySettings());
		}
		
		_actActivityLog = new ActivityLog();
		_actActivityLog.setEmailProvider("aol");
		_actActivityLog.setUserAccount(userAccount);
		_actActivityLog.setProxyUsed(proxyServer);
		_actActivityLog.setActivityServer(ResourceLoader.getServerName());
		
		try {
			conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("aol.conditiontimeout"));
		}catch(Exception ex) {
			conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("conditiontimeout"));
			
		}

		log.info("AOLReaderTask instance created " + userAccount + " Proxy " + userAccount.getProxySettings());
	}
	
	public Integer call() throws Exception {
		log.debug("call() Starts");
		
		initDrivers();
		if(login()) {
			/*driver.findElement(By.linkText("Read Mail")).click();
			delay(ExpectedConditions.titleContains("AOL"));
			driver.findElement(By.id("inboxNode")).click();
			delay(ExpectedConditions.titleContains("AOL"));*/

			driver.get("http://mail.aol.com/37834-111/aol-6/en-us/Lite/Today.aspx?src=bandwidth");
			delay(ExpectedConditions.titleContains("AOL"));
			
			processJunkMails();
			
			if(isInboxProcessingRequired()) {
				gotoInbox();
				processInbox();
			}
			
		}
		return 1;
	}
	
	
	private void processInbox() {
		log.debug("processInbox start");
		int numberOfEmailsPerPage = 30;
		for(int i=1; i<= numberOfEmailsPerPage; i++) {
			try {
				WebElement tableRow = driver.findElement(By.id("row"+i));
				String cssClass = tableRow.getAttribute("class");
				if(cssClass.contains("row-unread")) {
					List<WebElement> cells = tableRow.findElements(By.tagName("td"));
					try {
						String from = cells.get(2).getAttribute("title");
						String subject = cells.get(4).getText();
						 
						if(ismailIdentifire(from)) {
							
							readEmail(tableRow, subject, from);
						}
						processInbox();
					}catch(IndexOutOfBoundsException iob) {}
				}
			}catch(NoSuchElementException nse) {
				break;
			}
			catch(Exception ex) {
			}
		}
		
		if(isInboxProcessingRequired()) {
			if(gotoNextPage()) {
				processInbox();
			}
		}
		log.debug("processInbox ends");
	}
	
	private  void processJunkMails() {
		log.debug("processJunkMails start");
		if(isSpamProcessingRequired()) {
			driver.findElement(By.id("SpamLnk")).click();
			delay(ExpectedConditions.titleContains("Spam"));
			
			int numberOfEmailsPerPage = 30;
			for(int i=1; i<= numberOfEmailsPerPage; i++) {
				try {
					WebElement tableRow = driver.findElement(By.id("row"+i));
					try {
						tableRow.findElement(By.id("row"+i+"item1")).click();
					}catch(Exception ex) {
						
					}
					
				}catch(NoSuchElementException nse) {
					break;
				}
			
			}
			
			try {
				driver.findElement(By.id("markNotSpamBtn")).click();
				delay(ExpectedConditions.titleContains("Spam"));
			}catch(Exception ex) {
				
			}
			
		}
		log.debug("processJunkMails ends");
	}
	
	private boolean gotoNextPage() {
		log.debug("gotoNextPage starts");
		try {
			 WebElement nextPage = driver.findElement(By.id("nextPage"));
			 nextPage.click();
			 delay(ExpectedConditions.titleContains("Inbox"));
			 return true;
		}catch(Exception ex) {
		}
		log.debug("gotoNextPage ends");
		return false;
	}
	
	private void gotoInbox() {
		log.debug("gotoInbox starts");
		driver.findElement(By.id("InboxLnk")).click();
		delay(ExpectedConditions.titleContains("AOL"));
		log.debug("gotoInbox ends");
	}
	
	private void readEmail(WebElement element, String subject, String from) {
		log.debug("readEmail start");
		ActivityDetailLog detailLog = new ActivityDetailLog();
		long methodStart = System.currentTimeMillis();
		element.click();
		delay(ExpectedConditions.titleContains("Message View"));
		
		try {
			WebElement div=  driver.findElement(By.id("removedImage"));
			div.findElement(By.linkText("Show images")).click();
			threadSleep(5);
		}catch(NoSuchElementException nse) {}
		catch(Exception ex) {}
		
		try {
			 WebElement msgDiv = driver.findElement(By.id("_aolWebSuiteMsgBody"));
			 
			 String range = "";
			 int min = 5;
			 int max = 10;
			 try {
					range = ResourceLoader.getConfigValue("aol.readtime");
			 }catch(Exception ex) {
					range = ResourceLoader.getConfigValue("readtime");
			 }
			 try {
					String rparts[] = range.split(",");
					min = Integer.parseInt(rparts[0]);
					max = Integer.parseInt(rparts[1]);
				}catch(Exception ex) {
					min = 5;
					max = 10;
				}
				
				try {
					int wait = RangeRandom.next(min, max);
					log.info("Spending time in reading message " + wait);
					Thread.sleep(1000 * wait);
					log.info("Message read time over");
				}catch(Exception ex) {
					ex.printStackTrace();
				}
			
				try {
					WebElement link = msgDiv.findElement(By.linkText(clickLinkIdentifire));
					if(null != link) {
						detailLog.setLink(link.getAttribute("href"));
						
						String mainWindowHandle = driver.getWindowHandle();
						link.click();
						threadSleep(5);
						Set<String> handles =  driver.getWindowHandles();
						 
						if(handles.size() > 1) {
							Iterator<String> iterator = handles.iterator();
							while (iterator.hasNext()) {
								String handle = iterator.next();
								//System.out.println(handle);
								if(!mainWindowHandle.equalsIgnoreCase(handle)) {
									driver.switchTo().window(handle);
									try {
										log.info("Spending time in reading web page");
										//Thread.sleep(1000 * RangeRandom.next(min, max));
										Thread.sleep(1000 *2);
										log.info("Web page read time over");
									}catch(Exception ex) {
										ex.printStackTrace();
									}

									driver.close();
								}
							}
						}
						driver.switchTo().window(mainWindowHandle);
					}
				}catch(Exception ex){}
				
				detailLog.setSubject(subject);
				detailLog.setFrom(from);
				
				try {
					String fparts[] = from.split("@");
					detailLog.setSenderDomain(fparts[1]);
				}catch(Exception ex) {
					detailLog.setSenderDomain("");
					ex.printStackTrace();
				}
			 
		}catch(Exception ex) {}
		finally {
			long methodEnd = System.currentTimeMillis();
			long elapsedTime = methodEnd - methodStart;
			detailLog.setProcessingTime(elapsedTime);
			_actActivityLog.addDetail(detailLog);
			
		}
		//gotoInbox();
		
		try {
			driver.findElement(By.id("closeMsg")).click();
			delay(ExpectedConditions.titleContains("Inbox"));
		}catch(Exception ex){}
		
		
		log.debug("readEmail ends");
	}
	
	private boolean ismailIdentifire(String lable) {
		log.debug("ismailIdentifire starts with lable " +lable);
		if(_multiMailIdentifire.contains("*")) {
			return true;
		}
		for(String str : _multiMailIdentifire) {
			if(lable.contains(str)) {
				log.debug("ismailIdentifire ends true");
				return true;
			}
		}
		log.debug("ismailIdentifire ends false");
		return false;
	}
	
	private boolean login() {
		log.debug("login() starts");
		driver.get("http://mail.aol.com/");
	/*	delay(ExpectedConditions.titleContains("AOL"));
		driver.findElement(By.partialLinkText("SIGN IN")).click();*/
		delay(ExpectedConditions.titleContains("AOL"));
		
		WebElement query = driver.findElement(By.id("lgnId1"));
		query.sendKeys(userAccount.getUserName());
		query = driver.findElement(By.id("pwdId1"));
		query.sendKeys(userAccount.getPassword());
		query = driver.findElement(By.id("submitID"));
		query.click();
		delay(ExpectedConditions.titleContains("AOL"));
		try {
			query = driver.findElement(By.id("snPwdErr"));
			if(query.isDisplayed()) {
				String error = query.getText();
				_actActivityLog.setFailMessage(error);

				if(error.contains("Incorrect ")) {
					return false;
				}
			}
			
		}catch(Exception ex) {}
		
		try {
		 driver.findElement(By.partialLinkText("Sign Out"));
		 return true;
		}catch(Exception ex) {}
		
		return true;
		
	}
	
	
	
	private boolean isInboxProcessingRequired() {
		 log.debug("isInboxProcessingRequired start");
		 WebElement element = driver.findElement(By.id("InboxLnk"));
		 String text =  element.getText();
		 text = text.replace("\n", "");
		 log.debug("isInboxProcessingRequired ends.");
		 return text.matches(".*\\d.*");
	}
	
	private boolean isSpamProcessingRequired() {
		 log.debug("isSpamProcessingRequired start");
		 try {
			 WebElement element = driver.findElement(By.id("SpamCount"));
			 log.debug("isSpamProcessingRequired ends.");
			 return true;
		 }catch(NoSuchElementException nse) {
			 log.debug("isSpamProcessingRequired ends.");
			 return false;
		 }
	}
	
	private void delay(ExpectedCondition<?> condition) {
		WebDriverWait wait = new WebDriverWait(driver, 60);
		log.info("wating for " + condition.toString());
		wait.until(condition);
		log.info("Done wating for "  + condition.toString());
	}
	private void threadSleep(int second) {
		try {
			log.info("wating for deletion s:" +second);
			Thread.sleep(1000 * second);
			log.info("Wait over");
		}catch(Exception ex) {
		}
	}
}
