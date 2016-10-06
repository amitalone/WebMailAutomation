package com.alacance.webMailAutomation.tasks;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.alacance.webMailAutomation.UserAccount;
import com.alacance.webMailAutomation.dao.LogDAO;
import com.alacance.webMailAutomation.dblogging.ActivityDetailLog;
import com.alacance.webMailAutomation.dblogging.ActivityLog;
import com.alacance.webMailAutomation.dblogging.DBLogThreadManager;
import com.alacance.webMailAutomation.util.CAPTCHAUtil;
import com.alacance.webMailAutomation.util.RangeRandom;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class GmailTaskThread implements Callable<Integer>{
	WebDriver driver;
	private final Logger log = Logger.getLogger(GmailTaskThread.class);
	private final UserAccount userAccount;
	//private static  String mailIdentifire;
	private static  String clickLinkIdentifire;
	private int spamCount =0;
	private int readCount =0;
	private ActivityLog _actActivityLog = null;
	long startTime = 0;
	int conditionTimeOut = 60;
	private List<String> _multiMailIdentifire;
	private int numberOfPagesToRead =0;
	private final int GMAIL_MAILS_PER_PAGE = 50;
	

	/**
	 * Constructor
	 */
	public GmailTaskThread(UserAccount userAccount) {
		this.userAccount = userAccount;
	}
	
	private boolean ismailIdentifire(String lable) {
		log.debug("ismailIdentifire starts with lable " +lable);
		for(String str : _multiMailIdentifire) {
			if(lable.contains(str)) {
				log.debug("ismailIdentifire ends true");
				return true;
			}
		}
		log.debug("ismailIdentifire ends false");
		return false;
	}
	private void initDrivers() {
		startTime = System.currentTimeMillis();
		
		//mailIdentifire = ResourceLoader.getConfigValue("gmail.mailIdentifire");
		_multiMailIdentifire = ResourceLoader.getGmailMultiMailIdentifire();
		clickLinkIdentifire = ResourceLoader.getConfigValue("gmail.clickLinkIdentifire");
		String proxyServer ="";
		try {
			numberOfPagesToRead = Integer.parseInt(ResourceLoader.getConfigValue("gmail.readpages"));
		}catch(Exception e) {
			// Catch Number format & null pointer exception
			numberOfPagesToRead = 0;
			e.printStackTrace();
		}
		
		if(null == userAccount.getProxySettings()) {
			driver = new FirefoxDriver();
			log.debug("Creating FX driver with no proxy");
		}else {
			FirefoxProfile profile = new FirefoxProfile();
/*			profile.setPreference("network.proxy.http", userAccount.getProxySettings().getHost());
			profile.setPreference("network.proxy.http_port", userAccount.getProxySettings().getPort());
*/			
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
		_actActivityLog.setEmailProvider("gmail");
		_actActivityLog.setUserAccount(userAccount);
		_actActivityLog.setProxyUsed(proxyServer);
		_actActivityLog.setActivityServer(ResourceLoader.getServerName());
		
		try {
			conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("gmail.conditiontimeout"));
		}catch(Exception ex) {
			conditionTimeOut = Integer.parseInt(ResourceLoader.getConfigValue("conditiontimeout"));
			
		}

		log.info("GmailTaskThread instance created " + userAccount + " Proxy " + userAccount.getProxySettings());
	}
	/**
	 * Thread run method.
	 */
	public Integer call() throws Exception {
		log.debug("call() Starts");
		
		initDrivers();
//		new Actions(driver).sendKeys(Keys.CONTROL,Keys.DIVIDE).build().perform();
		int pagesProccesd = 1;
		try {
			
			if(login()) {
				_actActivityLog.setStatus(1);
			    log.debug(userAccount.getUserName() + " Logged in");
				if(!ResourceLoader.gmailLoginOnly()) {
					
					verifySuspiciousActivity();
				    
				    checkIntro();

				    if(ResourceLoader.emptyGmailInbox()) {
						log.debug("Empty inbox mode detected");
						emptyInbox();
				    } else {
				    	if(isSpamProcessingRequired()) {
							log.debug(userAccount.getUserName() + " Spam processing required");
							gotoJunk();
							processJunkMails();
						}else {
							log.debug(userAccount.getUserName() + " Spam processing NOT required");
							
						}
						if(isInboxProccesingRequired()) {
							log.debug(userAccount.getUserName() + " Inbox processing required");
							gotoInbox();
							processInbox();
							
							for(int counter=1; counter<=numberOfPagesToRead;counter++) {
								clickOlderButton();
								if(isInboxProccesingRequired()) {
									processInbox();
								}
							}
							
							// Reiterate Once to check if any mails are left.
					/*		gotoInbox();
							
							if(isInboxProccesingRequired()){processInbox();}
							
							for(int counter=1; counter<=numberOfPagesToRead;counter++) {
								clickOlderButton();
								if(isInboxProccesingRequired()) {
									processInbox();
								}
							}*/
							
						}else {
							log.debug(userAccount.getUserName() + " Inbox processing NOT required");
						}
						_actActivityLog.setMailReadCount(readCount);
						_actActivityLog.setSpamCount(spamCount);
				    }
				}
				
			}else {
			    _actActivityLog.setStatus(0);
			    log.debug(userAccount.getUserName() + " Logged FAILED");
			}
		}catch (Exception ex) {
			_actActivityLog.setStatus(0);
			ex.printStackTrace();
			log.info("Unhandled Exception "+ ex.getMessage());
		}finally {
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    _actActivityLog.setProcessingTime(elapsedTime);
		    DBLogThreadManager.getInstance().submitJob(_actActivityLog);
			LogDAO.getInstance().updateJobStatus();

			if(ResourceLoader.closeDriver()) {
				try {
					Thread.sleep(ResourceLoader.gmailDelaybeforeDriverClose() * 1000);
				}catch(Exception ex) {
				}
				driver.close();
			}
			log.info("Window Closed");


		}
		log.debug("call() Ends");
		return 1;
	}
	
	/**
	 * Login to gmail.
	 */
	private boolean login() {
		
		log.debug("login() starts");
		driver.get("https://mail.google.com/mail/?tab=mm");
		
		WebElement query = driver.findElement(By.id("Email"));
		query.sendKeys(userAccount.getUserName());
		query = driver.findElement(By.id("Passwd"));
		query.sendKeys(userAccount.getPassword());
		query = driver.findElement(By.id("signIn"));
		query.click();
		
		try {
			delay(ExpectedConditions.titleContains("Inbox"));
			log.info("Login successfull for " +userAccount.getUserName());
		}catch(TimeoutException te) {
			
			log.info("Time out occured in login process. Possible login failure.");
			String title = driver.getTitle();
			
			if(title.contains("Error")) {
				log.info("Gmail Server down. Closing thread");
				_actActivityLog.setFailMessage("Gmail Server down.");
				return false;
			}
			if(title.contains("disabled")) {
				log.info("Account disabled. Closing thread");
				_actActivityLog.setFailMessage("Account has been disabled");
				return false;
			}
			
			
			// Account verification page
			if(title.contains("Google Accounts")) {
			
				//Check if phone number is asked
				
				try {
					log.debug("Checking if phone number is asked");
					WebElement element = driver.findElement(By.id("PhoneVerificationChallenge"));
					element.click();
					element = driver.findElement(By.id("phoneNumber"));
					element.sendKeys(userAccount.getRecoveryPhone());
					driver.findElement(By.id("submitChallenge")).click();
					delay(ExpectedConditions.titleContains("Inbox"));
					return true;
				}catch(NoSuchElementException nse) {
					
				}catch(TimeoutException toe) {
					
				}
				
				// Check Skip Page
				try {
					WebElement element = driver.findElement(By.id("send-code-cancel-button"));
					element.click();
					delay(ExpectedConditions.titleContains("Inbox"));
					return true;
				}catch (NoSuchElementException nse) {
					
				}
				
				// Check Additional Security
				try {
					log.debug("Checking if 'Additional Security' page.");
					WebElement element = driver.findElement(By.id("loginchallengecaptcha"));
					WebElement element2 = driver.findElement(By.id("CaptchaChallengeOptionContent"));
					element2 = element2.findElement(By.tagName("img"));
					String url = element2.getAttribute("src");
					log.debug("CAPTCHA Offered");
					String solved = new CAPTCHAUtil().solveCAPTCHA(url);
					element.sendKeys(solved);
					driver.findElement(By.id("submitChallenge")).click();
					
				}catch(NoSuchElementException nse) {
					log.debug("NOT A 'Additional Security' page.");
				}
				
				// Check if phone verification page.
				try {
					log.debug("Checking if 'Verify your mobile number' page.");
					driver.findElement(By.id("send-code-cancel-button")).click();
					try {
						delay(ExpectedConditions.titleContains("Inbox"));
						return true;
					}catch(TimeoutException tee) {
					}
				}catch(NoSuchElementException nse) {
					log.debug("Verification page was NOT 'Verify your mobile number' ");
				}
				
				try {
					driver.findElement(By.id("RecoveryEmailChallenge")).click();
					delay(ExpectedConditions.visibilityOfElementLocated(By.id("emailAnswer")));
					driver.findElement(By.id("emailAnswer")).sendKeys(userAccount.getRecoveryEmail());
					driver.findElement(By.id("submitChallenge")).click();

				}catch(NoSuchElementException nse) {
					log.debug("Not a Recovery Challenge page");
					try {
						driver.findElement(By.id("idvreenableinput")).sendKeys(userAccount.getRecoveryPhone());
						driver.findElement(By.id("next-button")).click();
						_actActivityLog.setFailMessage("Google IVR Verification presented");
						return false;

					}catch(NoSuchElementException nse2) {
						_actActivityLog.setFailMessage("Unknown failure");
						return false;
					}
					
				}
				try {
					delay(ExpectedConditions.titleContains("Inbox"));
				}catch(TimeoutException tee) {
					if(title.contains("Google Accounts")) {
						// Verification value failed
						_actActivityLog.setFailMessage("Google Account Verification value failed");
					}
					return false;
				}
			}
			
			if(title.contains("Email from Google")) {
				try {
					driver.findElement(By.id("errormsg_0_Passwd"));
					log.info("Closing thread. Reason: Credentials failed for " +userAccount.getUserName());
					_actActivityLog.setFailMessage("Credentials failed.");
					return false;
				}catch(NoSuchElementException nsee){
				}
				
				try {
						driver.findElement(By.xpath("//div[@class='captcha-img']"));
						log.info("CAPTCH Offered to " +userAccount.getUserName());
						int retry = 3;
						try {
							retry = Integer.parseInt(ResourceLoader.getConfigValue("gmail.captcharetry"));
						}catch(Exception ex) {
							try {
								retry = Integer.parseInt(ResourceLoader.getConfigValue("captcharetry"));
							}catch(Exception ex2) {
							}
						}
						if(!loginWithCAPTCHA(retry)) {
							log.info("Solving captcha failed for " + userAccount.getUserName() + " with " + userAccount.getPassword());
							_actActivityLog.setFailMessage("Solving captcha failed.");
							return false;
						}
				}catch(NoSuchElementException nsee){
				}
			}
		}
		
		return true;
	}

	private void emptyInbox() {
		String lable = getCurrentInboxLable();
		try {
			
			
			selectAllMails();
			clickMailMore();
			clickMarkAsRead();
			
			try {
				 Thread.sleep(5000);
			 }catch(Exception ex) {
				 ex.printStackTrace();
			 }
			/*selectAllMails();
			clickDeleteButton();
			try {
				 Thread.sleep(5000);
			 }catch(Exception ex) {
				ex.printStackTrace(); 
			 }*/
			_actActivityLog.setFailMessage(lable + " => Marked read all");
			
			
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}
		
	}
	
	private void selectAllMails() {
		WebElement element = driver.findElement(By.xpath("//span[@role='checkbox']"));
		element.click();
		
		try {
			List<WebElement> links = driver.findElements(By.xpath("//span[@role='link']"));
			for(WebElement link : links) {
				if(link.getText().contains("Select all")) {
					link.click();
					break;
				}
			}
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}
	}
	private void clickMailMore() {
		try {
			List<WebElement> buttons = driver.findElements(By.xpath("//div[@role='button']"));
			for(WebElement button : buttons) {
				if(button.getText().contains("More")) {
					button.click();
					break;
				}
			}
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}
	}
	
	private void clickDeleteButton() {
		try {
			List<WebElement> buttons = driver.findElements(By.xpath("//div[@role='button']"));
			for(WebElement button : buttons) {
				if(button.isDisplayed()) {
					//System.out.println(button.getAttribute("outerHTML"));
					if(button.getAttribute("outerHTML").contains("Delete")) {
						button.click();
						try{
							Alert alert = driver.switchTo().alert();
							alert.accept();
						}catch(Exception ex){ex.printStackTrace();}
						break;
					}
				}
			}
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}

	}
	private void clickMarkAsRead() {
		try {
			List<WebElement> menus = driver.findElements(By.xpath("//div[@role='menu']"));
			for(WebElement menu : menus) {
				if(menu.isDisplayed()) {
					try {
						List<WebElement> menuItems = driver.findElements(By.xpath("//div[@role='menuitem']"));
						for(WebElement menuitem : menuItems) {
							if(menuitem.isDisplayed()) {
								if(menuitem.getText().contains("Mark as read")) {
									if(menuitem.isDisplayed()) {
										menuitem.click();
										try{
											Alert alert = driver.switchTo().alert();
											alert.accept();
										}catch(Exception ex){ex.printStackTrace();}
									}
									break;
								}
							}
						}
						
					}catch(NoSuchElementException nse) {
						nse.printStackTrace();
					}
				}
			}
		}catch(NoSuchElementException nse) {
			nse.printStackTrace();
		}
	}
	
	private void checkIntro() {
		log.debug("checkIntro() starts");
		try {
			WebElement element = driver.findElement(By.id("inproduct-guide-modal"));
			log.debug("Intro found posssible first login.");
			List<WebElement> links = element.findElements(By.tagName("a"));
			for(WebElement link:links) {
				if(link.getText().contains("×")) {
					link.click();
					break;
				}
			}
		}catch(NoSuchElementException nse) {
			log.debug("No intro found");
		}
		
		try {
			WebElement element = driver.findElement(By.id("ok"));
			element.click();
			try {
				Thread.sleep(2000);
			}catch(Exception ex) {}
		}catch(NoSuchElementException nse){}
		catch(Exception ex){}
		
		log.debug("checkIntro() starts");
	}
	private void verifySuspiciousActivity() {
		log.debug("verifySuspiciousActivity() starts");
		
		try {
			String mainWindowHandle = driver.getWindowHandle();
			
			WebElement link = driver.findElement(By.linkText("Was it you?"));
			if(!link.isDisplayed())  {
				delay(ExpectedConditions.visibilityOf(link));
			}
			log.debug("Suspicious activity link displayed");
			link.click();
			
			try {
				delay(ExpectedConditions.titleContains("Login details"));
				log.debug("Verification page loaded.");
				Set<String> handles =  driver.getWindowHandles();
				 
				if(handles.size() > 1) {
					Iterator<String> iterator = handles.iterator();
					while (iterator.hasNext()) {
						String handle = iterator.next();

						if(!mainWindowHandle.equalsIgnoreCase(handle)) {
							log.debug("verification page focused");
							driver.switchTo().window(handle);
							List<WebElement> elements = driver.findElements(By.xpath("//div[@role='button']"));
							for(WebElement element : elements) {
								if("Yes".equalsIgnoreCase(element.getText())) {
									log.debug("verify button found");
									element.click();
									break;
								}
							}
						}
					}
					
					log.debug("Switching back to inbox");
					driver.switchTo().window(mainWindowHandle);
				}
				
			}catch(TimeoutException te) {
			}
		}catch(NoSuchElementException nse) {
			
		}
		log.debug("verifySuspiciousActivity() ends");
	}
	/**
	 * Login with captcha.
	 */
	private boolean loginWithCAPTCHA(int retry) {
		log.debug("loginWithCAPTCHA statrts.");
		
		for(int i=0; i< retry; i++) {
			
			if(driver.getTitle().contains("Email from Google")) {
				log.info("Solving captcha trial " + (i+1));
				WebElement element =  driver.findElement(By.xpath("//div[@class='captcha-img']"));
				String captchaUrl = element.findElement(By.tagName("img")).getAttribute("src");
				String captchaText = new CAPTCHAUtil().solveCAPTCHA(captchaUrl); 
				
				WebElement query = driver.findElement(By.id("Email"));
				query.sendKeys(userAccount.getUserName());
				query = driver.findElement(By.id("Passwd"));
				query.sendKeys(userAccount.getPassword());
				query = driver.findElement(By.id("logincaptcha"));
				query.sendKeys(captchaText);
				query = driver.findElement(By.id("signIn"));
				query.click();
				
				try {
					delay(ExpectedConditions.titleContains("Inbox"));
					log.info("Captcha solved in attempt " + (i+1));
					
					log.debug("loginWithCAPTCHA ends.");
					
					return true;
				}catch(TimeoutException te) {
					log.info("Captcha Failed for tial " + (i+1));
				}
			}

		}
		log.debug("loginWithCAPTCHA ends.");
		return false;
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
			  element  = driver.findElement (By.partialLinkText("Spam"));
			  log.debug("isSpamProcessingRequired ends.");
			  return element.getText().contains("(");
		}catch(NoSuchElementException nse) {
			 // Looking for SPAM link lable not listed click on more button.
			if(findMore()) {
				 try {
					 // Recheck spam lable
					  element  = driver.findElement (By.partialLinkText("Spam"));
					  log.debug("isSpamProcessingRequired ends.");
					  return element.getText().contains("(");
				 }catch(NoSuchElementException nse2) {
				 }
			}
			// Forcefully load SPAM folder
			driver.get("https://mail.google.com/mail/u/0/?shva=1#spam");
			log.debug("isSpamProcessingRequired ends.");

			return true;
		}
	}
	
	private boolean findMore() {
		log.debug("findMore() starts");
		List<WebElement> elements = driver.findElements(By.xpath("//span[@role='button']"));
		for(WebElement elemnt : elements) {
			if(elemnt.getText().contains("More")) {
				log.debug("More button found");
				elemnt.click();
				log.debug("findMore() ends");
				return true;
			}
		}
		log.debug("findMore() ends");
		return false;
	}
	
	private boolean isInboxProccesingRequired() {
		log.debug("isInboxProccesingRequired start");
		WebElement  element  = driver.findElement (By.partialLinkText("Inbox"));
		log.debug("isInboxProccesingRequired ends.");
		return element.getText().contains("(");
	}
	
	private String getCurrentInboxLable() {
		log.debug("getCurrentInboxLable start");
		WebElement  element  = driver.findElement (By.partialLinkText("Inbox"));
		log.debug("getCurrentInboxLable ends.");
		return element.getText();
	}
	
	private boolean gotoJunk() {
		log.debug("gotoJunk start");
		List<WebElement> elements = driver.findElements(By.partialLinkText("Spam"));
		for(WebElement element : elements) {
			if(element.getAttribute("href").contains("#spam")) {
				element.click();
				try {
					delay(ExpectedConditions.titleContains("Spam"));
				}catch (TimeoutException te) {
					log.info("Unable to load Spam folder");
					log.debug("gotoJunk ends");
					return false;
				}
			}
		}
		log.debug("gotoJunk ends");
		return true;
	}
	
	private void gotoInbox() {
		log.debug("gotoInbox start");
		if(!driver.getTitle().contains("Inbox")) {
			List<WebElement> elements = driver.findElements(By.partialLinkText("Inbox"));
			for(WebElement element : elements) {
				if(element.getAttribute("href").contains("#inbox")) {
					element.click();
					delay(ExpectedConditions.titleContains("Inbox"));
				}
			}
		}
		
		log.debug("gotoInbox ends");
	}
	
	private  void processJunkMails() {
		log.debug("processJunkMails start");
		List<WebElement> elements = driver.findElements(By.xpath("//table[@class='F cf zt']"));
		boolean spamSelected = false;
		for(WebElement element : elements) {
			if(!StringUtils.isEmpty(element.getText())) {
				List<WebElement> mailRows = element.findElements(By.tagName("tr"));
				for(WebElement mailRow : mailRows) {
					List<WebElement> tableCells = mailRow.findElements(By.tagName("td"));
					if(tableCells.size() > 4) {
						WebElement fromCell = tableCells.get(4);
						WebElement span = fromCell.findElement(By.tagName("span"));
						if(null != span) {
							String email = span.getAttribute("email");
							//email.contains(mailIdentifire)
							if(ismailIdentifire(email)) {
								tableCells.get(1).click();
								spamSelected = true;
								spamCount++;
							}
						}
						
					}
				}
			}
		}
		
		if(spamSelected) {
			elements = driver.findElements(By.xpath("//div[@role='button']"));
			for(WebElement element : elements) {
				if("Not spam".equalsIgnoreCase(element.getText())) {
					element.click();
					break;
				}
			}
		}
		log.debug("processJunkMails ends");
	}
	
	private void processInbox() {
		/*try {
			log.info("Wating for 10 Seconds after login");
			Thread.sleep(10000);
		}catch(Exception ex){}*/
		log.debug("processInbox start");
		try {
			//List<WebElement> elements = driver.findElements(By.xpath("//table[@class='F cf zt']"));
			List<WebElement> elements = driver.findElements(By.cssSelector("table.F.cf.zt"));
			for(WebElement element : elements) {
				
				if(element.isDisplayed()) {
					try{

						if(!StringUtils.isEmpty(element.getText())) {
							List<WebElement> mailRows = element.findElements(By.tagName("tr"));
							log.debug("==========> MAILROWS" +mailRows.size());
							List<WebElement> tableCells = null;
							for(WebElement mailRow : mailRows) {
								try {
								    tableCells = mailRow.findElements(By.tagName("td"));
								}catch (Exception ex) {
									log.debug("******************************* EXCEPTION OCCURED IN LOCATING INBOX");
									/*log.debug("********************** title>>"+ driver.getTitle());
									try {
										log.debug("WATING 2 Seconds");
										Thread.sleep(2000);
									}catch(Exception ex2) {
										ex2.printStackTrace();
									}
									continue;
									//processInbox();
*/								}
								if(null!=tableCells && tableCells.size() > 4) {
									WebElement fromCell = tableCells.get(4);
									WebElement span =null;
									try {
										 span = fromCell.findElement(By.tagName("span"));
									}catch(Exception ex) {
										log.debug("======================================== EMAIL SPAN NOT FOUND");
									//	processInbox(); DANGER DONT UNCOMMENT
									}
									if(null != span) {
										String email = span.getAttribute("email");
										
											WebElement subjectCell = tableCells.get(5);
											span = subjectCell.findElement(By.tagName("span"));
											//Unread email
											if(span.getAttribute("innerHTML").contains("<b>")) {
												if(ismailIdentifire(email)) {
												String subjectLine = span.getText();
												readCount++;
												try {
													readEmail(subjectCell, subjectLine, email);
												}catch(Exception ex){log.debug("POSSIBLE ERROR IN READING."); ex.printStackTrace();}
												if(!backToInboxButton()) {
													gotoInbox();
												}
												
												//TODO: Remove this line after verification
												// IN QUESTION
												processInbox();
											 }
											}
										//}
									}
								}
							}
						}
					
					}catch(Exception ex) {
						// DONT BREAK LOOK ON ANY EXCEPTION.
						log.debug(ex);
						ex.printStackTrace();
					}
					
				
				}
				
			}
		}catch(NoSuchElementException nse) {
			log.debug("Element NOT found in Inbox processing");
			log.debug(nse);
			nse.printStackTrace();
		}catch(StaleElementReferenceException ste) {
			log.debug("STALE element refrence in Inbox processing");
			log.debug(ste);
			ste.printStackTrace();
		}catch(Exception ex) {
			log.debug("STALE element refrence in Inbox processing");
			log.debug(ex);
			ex.printStackTrace();
		}
		log.debug("processInbox ends");
	}
	
	private void readEmail(WebElement element, String subject, String from) {
		log.debug("readEmail start");
		ActivityDetailLog detailLog = new ActivityDetailLog();
		long methodStart = System.currentTimeMillis();
		if(element.isDisplayed()) {
			element.click();
			if(subject.length() > 90) {
				subject = subject.substring(0, 90);
			}
			delay(ExpectedConditions.titleContains(subject));
			String url = driver.getCurrentUrl();
			String parts[] = url.split("/");
			String id = parts[parts.length - 1];
			try {
					try {
						WebElement displayImages = driver.findElement(By.xpath("//span[contains(text(),'Display images below')]"));
						displayImages.click();
						log.info("Dispalying images in email");
					}catch(NoSuchElementException nsee) {
						log.debug("Images are not present in email");
					}
					
					//Cause delay in reading message;
					
					String range = "";
					int min = 5;
					int max = 10;
					try {
						range = ResourceLoader.getConfigValue("gmail.readtime");
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
						ex.printStackTrace();
					}
					
					
					detailLog.setSubject(subject);
					detailLog.setFrom(from);
					
					try {
						String fparts[] = from.split("@");
						detailLog.setSenderDomain(fparts[1]);
					}catch(Exception ex) {
						detailLog.setSenderDomain("");
						ex.printStackTrace();
					}
					
					
					try {
					 //	element =  driver.findElement(By.xpath("//div[@class='ii gt m"+id+" adP adO']"));
						element = driver.findElement(By.cssSelector("div.ii.gt.m"+id+".adP.adO"));
						try {
							element  = element.findElement(By.linkText(clickLinkIdentifire));
							
						}catch(Exception ex) {
							log.debug("=======================================CLICK IDENTIFIRE NOT FOUND");
							element = null;
						}
						
						
						if(null != element) {
							detailLog.setLink(element.getAttribute("href"));
							
							String mainWindowHandle = driver.getWindowHandle();

							element.click();
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

					}catch(Exception ex){
						ex.printStackTrace();
					}
					
					
					
			}catch(Exception ex) {
				ex.printStackTrace();
			}finally {
				long methodEnd = System.currentTimeMillis();
				long elapsedTime = methodEnd - methodStart;
				detailLog.setProcessingTime(elapsedTime);
				_actActivityLog.addDetail(detailLog);
			}
		}
		log.debug("readEmail ends");
	}
	
	private boolean backToInboxButton() {
		log.debug("backToInboxButton starts");
		try {
			WebElement element = driver.findElement(By.cssSelector("DIV.T-I.J-J5-Ji.lS.T-I-ax7.ar7"));
	 		element.click();
			delay(ExpectedConditions.titleContains("Inbox")) ;
			log.debug("backToInboxButton ends");
			return true;
		}catch(Exception ex) {
			//ex.printStackTrace();
			if(driver.getTitle().contains("Inbox")) {
				return true;
			}
			// Retry again
			try {
				WebElement element = driver.findElement(By.cssSelector("DIV.ar6.T-I-J3.J-J5-Ji"));
				if(element.isDisplayed()) {
					element.click();
					delay(ExpectedConditions.titleContains("Inbox")) ;
					return true;
				}
				
			}catch(Exception ex2) {
				log.debug("backToInboxButton ends");
				return false;
			}
		}
		log.debug("backToInboxButton ends");
		return false;
	}
	
	private void clickOlderButton() {
		log.debug("clickOlderButton starts");
		try {
/*			try {
				processInbox();
			}catch(Exception ex){}
*/	
			List<WebElement>  pagerDivs  = driver.findElements(By.cssSelector("DIV.ar5.J-J5-Ji"));
			 for(WebElement pagerDiv : pagerDivs) {
				 List<WebElement> divs = pagerDiv.findElements(By.tagName("div"));
				 for(WebElement div :divs) {
					 if(div.isDisplayed()) {
						 if(div.getAttribute("outerHTML").contains("Older")) {
							 div.click();
							 try {
								 Thread.sleep(3000);
							 }catch(Exception ex) {
								 ex.printStackTrace();
							 }
							 return;
						 }
					 }
				 }

			 }
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		log.debug("clickOlderButton ends");
	}
}
