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
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.alacance.webMailAutomation.NameDVO;
import com.alacance.webMailAutomation.ProxySettings;
import com.alacance.webMailAutomation.dao.NameDAO;
import com.alacance.webMailAutomation.dao.ProxyDataDAO;
import com.alacance.webMailAutomation.dao.SeedAccountDAO;
import com.alacance.webMailAutomation.dblogging.SeedAccountDVO;
import com.alacance.webMailAutomation.util.AccountCreatorUtil;
import com.alacance.webMailAutomation.util.CAPTCHAUtil;
import com.alacance.webMailAutomation.util.ResourceLoader;


public class GmailAccountCreatorTask implements Callable<Integer>{
	WebDriver driver;
	private final Logger log = Logger.getLogger(GmailAccountCreatorTask.class);
	private ProxySettings proxySettings = null;
	private SeedAccountDVO seedAccount = null;
	
	public GmailAccountCreatorTask(ProxySettings proxy) {
		this.proxySettings = proxy;
	}
	
	private void initDrivers() {

		if(null == proxySettings) {
			driver = new FirefoxDriver();
			log.debug("Creating FX driver with no proxy");
		}else {
			FirefoxProfile profile = new FirefoxProfile();
			Proxy proxy = new Proxy();
			String proxyServer = proxySettings.getHost() + ":"+proxySettings.getPort();
			proxy.setHttpProxy(proxyServer);
			proxy.setFtpProxy(proxyServer);
			proxy.setSslProxy(proxyServer);
			//profile.setProxyPreferences(proxy);
			
			driver = new FirefoxDriver(profile);
			log.debug("Creating FX driver with proxy " + proxySettings);
		}
		
		 
	}

	public Integer call() throws Exception {
		initDrivers();
		gotoSignup();
		fillForm();
		submitForm();
		return 1;
	}
	
	private void gotoSignup() {
		log.debug("gotoSignup() starts");
		driver.get("http://google.com");
		
		delay(ExpectedConditions.titleContains("Google"));
		driver.findElement(By.xpath("//span[text()='Sign in']")).click();
		delay(ExpectedConditions.titleContains("Google Accounts"));
		driver.findElement(By.id("link-signup")).click();
		delay(ExpectedConditions.titleContains("Google Accounts"));
		log.debug("gotoSignup() ends");
	}
	
	private void delay(ExpectedCondition<?> condition) {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		log.info("wating for " + condition.toString());
		wait.until(condition);
		log.info("Done wating for "  + condition.toString());
	}
	
	private void submitForm() {
		driver.findElement(By.id("submitbutton")).click();
		System.out.println(seedAccount.getPassword());

		/*try {
			delay(ExpectedConditions.titleContains("Welcome"));
		}catch(TimeoutException tse) {
			if(driver.getTitle().contains("Accounts")) {
				log.debug("Submission failed");
				
				try {
					WebElement element = driver.findElement(By.id("errormsg_0_signupcaptcha"));
					if(element.isDisplayed()) {
						if(element.getText().length() > 0) {
							log.info("CAPTCHA Failed");
							
							String captcha = getCaptcha();
							driver.findElement(By.id("Passwd")).sendKeys(seedAccount.getPassword());
							driver.findElement(By.id("PasswdAgain")).sendKeys(seedAccount.getPassword());
							driver.findElement(By.id("recaptcha_response_field")).sendKeys(captcha);
							seedAccount.setCaptcha(captcha);
							submitForm();
						}
					}
				}catch(NoSuchElementException nse) {
					
				}
			}
		}*/
		
		int id = SeedAccountDAO.getInstance().saveAccount(seedAccount);
		SeedAccountDAO.getInstance().updateAccountStatus(id);
		ProxyDataDAO.getInsatnce().updateProxyStatus(proxySettings.getHost());
	}
	private void fillForm() {
		seedAccount = new SeedAccountDVO();
		NameDVO name = NameDAO.getInstance().getRandomName();
		
		driver.findElement(By.id("FirstName")).sendKeys(name.getFirst());
		seedAccount.setFname(name.getFirst());
		driver.findElement(By.id("LastName")).sendKeys(name.getLast());
		seedAccount.setLname(name.getLast());
		
		String emailId = name.getEmail();
		
		emailId = emailId.toLowerCase();
		driver.findElement(By.id("GmailAddress")).sendKeys(emailId);
		
		driver.findElement(By.id("Passwd")).click();
		threadSleep(2);
		
		try {
			if(driver.findElement(By.id("errormsg_0_GmailAddress")).isDisplayed()) {
				emailId = ResourceLoader.getSuffix()+name.getEmail();
				emailId = emailId.toLowerCase();
				
				driver.findElement(By.id("GmailAddress")).clear();
				
				driver.findElement(By.id("GmailAddress")).sendKeys(emailId);
				driver.findElement(By.id("Passwd")).click();
				threadSleep(2);
			}
			if(driver.findElement(By.id("errormsg_0_GmailAddress")).isDisplayed()) {
				List<WebElement> links = driver.findElement(By.id("username-suggestions")).findElements(By.tagName("a"));
				Collections.shuffle(links);
				WebElement link = links.get(0);
				emailId = link.getText();
				link.click();
			}
		}catch(NoSuchElementException nse) {
			//System.out.println(nse);
		}
		seedAccount.setUsername(emailId);
		
		driver.findElement(By.id("Passwd")).clear();
		String pass = ResourceLoader.getPassword();
		
		driver.findElement(By.id("Passwd")).sendKeys(pass);
		driver.findElement(By.id("PasswdAgain")).sendKeys(pass);
		seedAccount.setPassword(pass);

		String monthLbl = AccountCreatorUtil.getMonth();
		selectMonth(monthLbl);
		seedAccount.setBmonth(monthLbl);
		
		int bday = AccountCreatorUtil.getDay();
		driver.findElement(By.id("BirthDay")).sendKeys(bday+"");
		seedAccount.setBday(bday);
		
		int byear = AccountCreatorUtil.getYear();
		driver.findElement(By.id("BirthYear")).sendKeys(byear+"");
		seedAccount.setByear(byear);
		
		String genderLbl = name.getGender();
		if("M".equalsIgnoreCase(genderLbl)) {
			genderLbl = "Male";
		}
		if("F".equalsIgnoreCase(genderLbl)) {
			genderLbl = "Female";
		}
		
		selectGender(genderLbl);
		seedAccount.setGender(genderLbl);
		
		
		String ph = ResourceLoader.getPhoneNumber();
		ph = "+91"+ph;
		driver.findElement(By.id("RecoveryPhoneNumber")).clear();
		driver.findElement(By.id("RecoveryPhoneNumber")).sendKeys(ph);
		seedAccount.setPhone(ph);
		
		String revoveryEmail = name.getEmail()+"@"+ResourceLoader.getDomain(); 
		revoveryEmail = revoveryEmail.toLowerCase();
		driver.findElement(By.id("RecoveryEmailAddress")).sendKeys(revoveryEmail);
		seedAccount.setCurrentemail(revoveryEmail);
		
		String captcha = getCaptcha();
		driver.findElement(By.id("recaptcha_response_field")).sendKeys(captcha);
		seedAccount.setCaptcha(captcha);
		
		String location = driver.findElement(By.id("CountryCode")).getText();
		seedAccount.setLocation(location);
		
		driver.findElement(By.id("TermsOfService")).click();
		driver.findElement(By.id("Personalization")).click();
		//seedAccount.setProxy(proxySettings.getHost() + ":" + proxySettings.getPort());
		//seedAccount.setProxyserver(proxySettings.getServerTag());
		
		
		
		
	}
	
	private String getCaptcha() {
		String url = driver.findElement(By.id("recaptcha_image")).findElement(By.tagName("img")).getAttribute("src");
		String captcha = new CAPTCHAUtil().solveCAPTCHA(url);
		return captcha;
	}
	
	private void selectMonth(String lable) {
		log.debug("selectMonth() starts");
		driver.findElement(By.id("BirthMonth")).findElement(By.cssSelector("div.goog-inline-block.goog-flat-menu-button-dropdown")).click();
		threadSleep(1);
		
		WebElement element = driver.findElement(By.id("BirthMonth"));
		element = element.findElement(By.cssSelector("div.goog-menu.goog-menu-vertical"));
		if(element.isDisplayed()) {
			List<WebElement> months =element.findElements(By.cssSelector("div.goog-menuitem-content"));
			for(WebElement mEle :months) {
				if(lable.equalsIgnoreCase(mEle.getText())) {
					mEle.click();
					return;
				}
			}
		}
		
		log.debug("selectMonth() ends");
	}
	
	private void selectGender(String lable) {
		log.debug("selectGender() starts");
		driver.findElement(By.id("Gender")).findElement(By.cssSelector("div.goog-inline-block.goog-flat-menu-button-dropdown")).click();
		threadSleep(1);
		
		WebElement element = driver.findElement(By.id("Gender"));
		element = element.findElement(By.cssSelector("div.goog-menu.goog-menu-vertical"));
		if(element.isDisplayed()) {
			List<WebElement> months =element.findElements(By.cssSelector("div.goog-menuitem-content"));
			for(WebElement mEle :months) {
				if(lable.equalsIgnoreCase(mEle.getText())) {
					mEle.click();
					return;
				}
			}
		}
		
		log.debug("selectGender() ends");
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
