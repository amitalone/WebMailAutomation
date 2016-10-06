package com.alacance.webMailAutomation.tasks;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.dao.LogDAO;
//import com.alacance.webMailAutomation.dao.LogDAO;
import com.alacance.webMailAutomation.dblogging.ActivityDetailLog;
import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.dblogging.DBLogThreadManager;
import com.alacance.webMailAutomation.util.GmailIMAP;
//import com.alacance.webMailAutomation.dblogging.DBLogThreadManager;
import com.alacance.webMailAutomation.util.RangeRandom;
import com.alacance.webMailAutomation.util.ResourceLoader;
 
public class HotmailTaskThread implements Callable<Integer>{

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
	
	public HotmailTaskThread(UserAccount userAccount) {
		this.userAccount = userAccount;
		
	}
	
	@SuppressWarnings("deprecation")
	private void initDrivers() {
		
		try {
			numberOfPagesToRead = Integer.parseInt(ResourceLoader.getConfigValue("hotmail.readpages"));
		}catch(Exception e) {
			// Catch Number format & null pointer exception
			numberOfPagesToRead = 0;
			e.printStackTrace();
		}
		mailIdentifire = ResourceLoader.getConfigValue("hotmail.mailIdentifire");
		clickLinkIdentifire = ResourceLoader.getConfigValue("hotmail.clickLinkIdentifire");
		if(null == userAccount.getProxySettings()) {
			driver = new FirefoxDriver();
			log.debug("Creating FX driver with no proxy");
		}else {
			FirefoxProfile profile = new FirefoxProfile();
			Proxy proxy = new Proxy();
			String proxyServer = userAccount.getProxySettings().getHost() + ":"+userAccount.getProxySettings().getPort();
			proxy.setHttpProxy(proxyServer);
			proxy.setFtpProxy(proxyServer);
			proxy.setSslProxy(proxyServer);
		//	profile.setProxyPreferences(proxy);
			
			driver = new FirefoxDriver(profile);
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
	
	public Integer call() throws Exception {
		initDrivers();
		 /*Actions act = new Actions(driver);
		act.sendKeys(Keys.CONTROL,Keys.DIVIDE);
		act.release();
		act = null; */
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
						gotoInbox();
						forceClean = forceClean();
						processInbox();
						
						for(int counter=1; counter<=numberOfPagesToRead;counter++) {
							 gotoNextPage();
							if(isInboxProccesingRequired()) {
								processInbox();
							} 
						}
					}
					 
				}
				
				if(emptyJunk) {
					emptyJunk();
				}
				if(emptyInbox) {
					emptyInbox();
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
	
	
	private void checkEmailVerification() {
		WebElement ele = driver.findElement(By.id("iProofOptions"));
		try {
			UserAccount verificationAccount = ResourceLoader.getVerificationId();
			
			Select select = new Select(ele);
			select.selectByValue("CSS");
			ele = driver.findElement(By.id("iNext"));
			ele.click();
			delay(ExpectedConditions.titleContains("I don't have my security info any more"));
			ele = driver.findElement(By.id("iNext"));
			ele.click();
			delay(ExpectedConditions.titleContains("Since you don't have access to your current security"));
			ele = driver.findElement(By.id("iProofOptions"));
			select = new Select(ele);
			select.selectByValue("Email");
			ele = driver.findElement(By.id("EmailAddress"));
			ele.sendKeys(verificationAccount.getUserName());
			ele = driver.findElement(By.id("iNext"));
			ele.click();
			
			delay(ExpectedConditions.titleContains("We just sent a code"));
			
			ele = driver.findElement(By.id("iOttText"));
			
			String code = null;
			try {
				log.debug("Sleeping for 10 seconds");
				Thread.sleep(1000* 10);
				log.debug("Awake");
				code = new GmailIMAP().readVerificationCode(verificationAccount.getUserName(), verificationAccount.getPassword(), userAccount.getUserName());
			}catch(Exception ex) {}
			
			ele.sendKeys(code);
			
			ele = driver.findElement(By.id("iNext"));
			ele.click();
			delay(ExpectedConditions.titleContains("One last step"));
			
			ele = driver.findElement(By.id("iNext"));
			ele.click();
			
			delay(ExpectedConditions.titleContains("Thanks"));
			
			ele = driver.findElement(By.id("iNext"));
			ele.click();
		
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	private void gotoNextPage() {
	
		 WebElement ele = driver.findElement(By.id("nextPageLink"));
		
		 // Scroll page to see next page button.
		 Actions builder = new Actions(driver);
		 builder.moveToElement(ele);
		 builder.perform();
		 
		 if(null != ele) {
			  ele = ele.findElement(By.tagName("img"));
			  ele.click();
			 // delay(ExpectedConditions.titleContains(userAccount.getUserName()));
				delay(ExpectedConditions.visibilityOf(driver.findElement(By.id("messageListContentContainer"))));

		 }
		 
	}
	private void forceCloseAllWindows() {
		try {
			Set<String> handles =  driver.getWindowHandles();
			if(handles.size() > 1) {
				Iterator<String> iterator = handles.iterator();
				while (iterator.hasNext()) {
					driver.close();
				}
			}
		}catch(Exception ex) {
			
		}
	}
	private boolean login() {
		log.debug("Entreing login");
		driver.get("http://outlook.com");
		WebElement query = driver.findElement(By.name("login"));
		query.sendKeys(userAccount.getUserName());
		query = driver.findElement(By.name("passwd"));
		query.sendKeys(userAccount.getPassword());
		query = driver.findElement(By.name("SI"));
		query.click();
		try {
			delay(ExpectedConditions.titleContains(userAccount.getUserName()));
			log.info("Login successfull for " +userAccount.getUserName());
			return true;
		}catch(TimeoutException te) {
			log.info("Time out occured in login process. Possible login failure.");
			String title = driver.getTitle();
			log.info("Timedout title " + driver.getTitle()); 
			if(title.contains("Call us overprotective")) {
				checkEmailVerification();
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
	
	
	private boolean forceClean() {
		log.debug("Entering forceClean()");
		WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000001"));
		String text = element.getText();
		text = text.replace("Inbox", "");
		text = text.trim();
		int numOfUnread =0;
		try {
			numOfUnread = Integer.parseInt(text);
			if(numOfUnread > 100) {
				emptyInbox();
				_actActivityLog.setFailMessage("Cleaning Inbox due to large unread emails " + numOfUnread);
				log.debug("Leaving forceClean()");
				return true;
			}
		}catch(NumberFormatException nfe) {
			numOfUnread =0;
		}
		log.debug("leaving forceClean()");
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
		if(!driver.getCurrentUrl().contains("fid=5")) {
			gotoJunk();
		}
		log.debug("gotoJunk ends");
		return true;
	}
	
	private  void processJunkMails() {
		log.debug("processJunkMails start");
		try {
			WebElement  element  = driver.findElement(By.id("messageListContentContainer"));
			List<WebElement> elements = element.findElements(By.tagName("li"));
			
			Collections.shuffle(elements);
			for(WebElement emailRow : elements) {
				if(emailRow.findElement(By.tagName("a")).getAttribute("class").contains("TextSemiBold")) {
					String email = emailRow.findElement(By.xpath("//span[@email]")).getAttribute("email");
					if(isMailIdentifire(email)) {
						//WebElement chkBox = emailRow.findElement(By.xpath("//input[@type='checkbox']"));
						

						WebElement chkBox = emailRow.findElement(By.tagName("input"));
						chkBox.click();
						
						delay(ExpectedConditions.visibilityOfElementLocated(By.id("MarkAsNotJunk")));
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
	private void gotoInbox() {
		log.debug("gotoInbox start");
/*		if(driver.getCurrentUrl().contains("&fid=1")) {
			log.debug("gotoInbox ends");
			return;
		}*/
		WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000001"));
		element.click();
		try {
		
			//delay(ExpectedConditions.titleContains(userAccount.getUserName()));
			delay(ExpectedConditions.visibilityOf(driver.findElement(By.id("messageListContentContainer"))));
		
		}catch(TimeoutException te) {
			log.info("Timeout occured in loading inbox");
		}
		log.debug("gotoInbox ends");
	}
	
	private boolean isInboxProccesingRequired() {
		log.debug("isInboxProccesingRequired start");
		WebElement  element  = driver.findElement(By.id("00000000-0000-0000-0000-000000000001"));
		//WebElement  element  = driver.findElement (By.partialLinkText("Inbox"));
		log.debug("isInboxProccesingRequired ends.");
		return element.getText().matches(".*\\d.*");
	}
	
	private void processInbox() {
		log.debug("processInbox start");
		if(!forceClean) {
			 try {
				 try {
						log.info("Wating 2 seconds");
						Thread.sleep(1000 * 1);
						log.info("Wait over");
					}catch(Exception ex) {
					}
				 
				 WebElement element  = driver.findElement(By.id("messageListInnerContainer"));
				
				List<WebElement> elements = element.findElements(By.tagName("li"));
				Collections.shuffle(elements);
				 
				for(WebElement emailRow : elements) {
					 
					try {
						//System.out.println(emailRow.getText());
						//System.out.println(emailRow.getAttribute("id"));
						
						element = emailRow.findElement(By.className("FmD"));
						System.out.println(element.getText());
						if(element.findElement(By.tagName("a")).getAttribute("class").contains("TextSemiBold")) {
							//String email = element.findElement(By.xpath("//span[@email]")).getAttribute("email");
							String email = element.findElement(By.tagName("span")).getAttribute("email");
							//System.out.println("EMAIL =========== " + email);
							if(isMailIdentifire(email)) {
								String subject = emailRow.findElement(By.className("Sb")).getText();
								//System.out.println("********************* "+subject);
								if(!"".equals(subject)) {
									readEmail(emailRow, subject, email);
									readCount++;
								}
								//gotoInbox();
								//processInbox();
								closeReadingPane();
							}
						}
						
					}catch(NoSuchElementException nse){
						//System.out.println(" NSE OCCURED");
					}catch(StaleElementReferenceException sere) {
						
					}
	 			}
				
			}catch(Exception nse) {
				log.debug("Error in processInbox ", nse);
			}
		}
		
		log.debug("processInbox ends");
	}
	
	private void closeReadingPane() {
		try {
			WebElement ele =  driver.findElement(By.cssSelector("img.g_close "));
			if(null !=  ele && ele.isDisplayed()) {
				ele.click();
				Thread.sleep(1000 * 2);
			}
		}catch(Exception ex) {
			//gotoInbox();
			//ex.printStackTrace();
		}
	}
	
	private void readEmail(WebElement element, String subject, String from) {
		log.debug("readEmail start");
		ActivityDetailLog detailLog = new ActivityDetailLog();
		long methodStart = System.currentTimeMillis();
		if(element.isDisplayed()) {
			element.click();
		} else {
			return;
		}
		
		/*try {
			delay(ExpectedConditions.presenceOfElementLocated(By.id("msgParts")));
		}catch(TimeoutException te) {
			log.debug("TE occured in locating msgParts");
		}*/
		try {
			WebElement showContent = driver.findElement(By.linkText("Show content"));
			showContent.click();
			try {
				log.info("Wating 5 seconds");
				Thread.sleep(1000 * 5);
				log.info("Wait over");
			}catch(Exception ex) {
			}
		}catch(NoSuchElementException nse) {
			log.debug("Show content button not present");
		}
		try {
			
			
			String range = "";
			int min = 5;
			int max = 10;
			try {
				range = ResourceLoader.getConfigValue("hotmail.readtime");
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
				log.info("Spending time in reading message");
				Thread.sleep(1000 * RangeRandom.next(min, max));
				log.info("Message read time over");
			}catch(Exception ex) {
			}
			
			checkContentUnblock();
			
			detailLog.setSubject(subject);
			detailLog.setFrom(from);
			
			try {
				String fparts[] = from.split("@");
				detailLog.setSenderDomain(fparts[1]);
			}catch(Exception ex) {
				detailLog.setSenderDomain("");
			}
			element = driver.findElement(By.id("msgParts"));
			
			// Random link
			
			String mainWindowHandle = driver.getWindowHandle();

			try {
				List<WebElement> links = driver.findElement(By.id("mpf0_readMsgBodyContainer")).findElements(By.tagName("a"));
				
				if("*".equalsIgnoreCase(clickLinkIdentifire)) {
					Collections.shuffle(links);
					for(WebElement link : links) {
						if(!(link.getText().contains("Unsub") || link.getText().contains("unsub") || link.getText().contains("UNSUB"))) {
							detailLog.setLink(link.getAttribute("href"));
							link.click();
							
							break;
						}
					}

				} else {
					for(WebElement link : links) {
						if(link.getText().contains(clickLinkIdentifire)) {
							link.click();
							break;
						}
					}
				}
				
			}catch(Exception ex) {
				element  = element.findElement(By.linkText(clickLinkIdentifire));
				detailLog.setLink(element.getAttribute("href"));
				
				element.click();
			}

			
			checkContentUnblock();
			
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
							Thread.sleep(1000 * RangeRandom.next(min, max));
							log.info("Web page read time over");
						}catch(Exception ex) {
						}

						driver.close();
						
					}
				}
			}
			driver.switchTo().window(mainWindowHandle);
			
		}catch(NoSuchElementException nse){
			log.debug("Error in read email ", nse);
		}catch (Exception ex) {
			log.debug("Error in read email ", ex);
		} 
		finally {
			long methodEnd = System.currentTimeMillis();
			long elapsedTime = methodEnd - methodStart;
			detailLog.setProcessingTime(elapsedTime);
			_actActivityLog.addDetail(detailLog);
		}
		
		log.debug("readEmail ends");
	}
	
	private void checkContentUnblock() {
		log.debug("checkContentUnblock starts");
		try {
			List<WebElement> buttons = driver.findElements(By.tagName("button"));
			for(WebElement button : buttons) {
				if(button.getText().contains("Unblock")) {
					button.click();
					try {
						Thread.sleep(1000 * 2);
					}catch(Exception ex) {
						
					}
				}
			}
		}catch(NoSuchElementException nse ) {
			
		}catch(StaleElementReferenceException sere) {
			
		}
		log.debug("checkContentUnblock ends");
	}
	
	private void emptyInbox() {
		log.debug("Entreing emptyInbox");
		gotoInbox();
		
		WebElement  element  = driver.findElement(By.id("folderListControlUl"));
		
		List<WebElement> listElements = element.findElements(By.tagName("li"));
		for(WebElement li : listElements) {
			if(li.getText().contains("Inbox")) {
				element = li;
				break;
			}
		}
		
		new Actions(driver).contextClick(element).perform();
		try {
			delay(ExpectedConditions.visibilityOfElementLocated(By.id("flcm")));
			element = driver.findElement(By.id("flcm_folder"));
			
			//if(element.getAttribute("style").contains("block")) {
				List<WebElement> lis = element.findElements(By.tagName("li"));
				
				
				for(WebElement li : lis) {
					//System.out.println("LI == " + li);
					
					if(li.getAttribute("key").equalsIgnoreCase("emptyFldr")) {
						delay(ExpectedConditions.visibilityOf(li));
						li.click();
						
						element = driver.findElement(By.tagName("Button"));
						if(element.getText().equalsIgnoreCase("Empty")) {
							element.click();
						}
						try {
							log.info("wating for deletion s:" + emptyInboxWait );
							Thread.sleep(1000 * emptyInboxWait);
							log.info("wait over");
						}catch(Exception ex) {
						}
						
						_actActivityLog.setFailMessage(_actActivityLog.getFailMessage() + " Empty Inbox finsihed");
					}
				}
			//}
			 
					
		}catch(NoSuchElementException nse) {
			
		}
		
		
		
		try {
			log.info("wating for context menu");
			Thread.sleep(1000 * 2);
			log.info(" time over");
		}catch(Exception ex) {
		}
		
		 
		log.debug("Leaving emptyInbox");

	}
	
	private void emptyJunk() {
		log.debug("Entreing emptyJunk");
		gotoJunk();
		
		try {
			WebElement element = driver.findElement(By.id("EmptyFolderButton"));
			element.click();
			try {
				log.info("wating for popup");
				Thread.sleep(1000 * 1);
				log.info(" time over");
			}catch(Exception ex) {
			}

		
			element = driver.findElement(By.tagName("Button"));
			if(element.getText().equalsIgnoreCase("Empty")) {
				element.click();
			}
			try {
				log.info("wating for deletion s:" +emptyJunkWait);
				Thread.sleep(1000 * emptyJunkWait);
				log.info("Wait over");
			}catch(Exception ex) {
			}
			_actActivityLog.setFailMessage("Deleted JUNK");
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}
		
		log.debug("Leaving emptyJunk");
	}
	
	 
	
	private void delay(ExpectedCondition<?> condition) {
		
		WebDriverWait wait = new WebDriverWait(driver, conditionTimeOut);
		log.info("wating for " + condition.toString());
		wait.until(condition);
		log.info("Done wating for "  + condition.toString());
	}
}
