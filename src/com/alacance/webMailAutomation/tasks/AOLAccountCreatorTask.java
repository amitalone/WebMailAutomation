package com.alacance.webMailAutomation.tasks;

import java.util.Collections;
import java.util.List;
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

import com.alacance.webMailAutomation.NameDVO;
import com.alacance.webMailAutomation.ProxySettings;
import com.alacance.webMailAutomation.dao.NameDAO;
import com.alacance.webMailAutomation.dao.ProxyDataDAO;
import com.alacance.webMailAutomation.dao.SeedAccountDAO;
import com.alacance.webMailAutomation.dblogging.SeedAccountDVO;
import com.alacance.webMailAutomation.util.AccountCreatorUtil;
import com.alacance.webMailAutomation.util.CAPTCHAUtil;
import com.alacance.webMailAutomation.util.ResourceLoader;

public class AOLAccountCreatorTask implements Callable<Integer>{
	
	WebDriver driver;
	private final Logger log = Logger.getLogger(AOLAccountCreatorTask.class);
	private ProxySettings proxySettings = null;
	private SeedAccountDVO seedAccount = null;
	
	public AOLAccountCreatorTask(ProxySettings proxy) {
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
		
		//driver.findElement(By.id("dltoolbar")).click();
		//driver.findElement(By.linkText("OK")).click();
		
		threadSleep(5);
		driver.findElement(By.id("signup-btn")).click();
		
		delay(ExpectedConditions.titleContains("AOL"));
		seedAccount.setProvider("AOL");
		
		try {
			WebElement element = driver.findElement(By.cssSelector("INPUT.button.signin-btn"));
			element.click();
			delay(ExpectedConditions.titleContains("AOL"));
			threadSleep(5);
			int id = SeedAccountDAO.getInstance().saveAccount(seedAccount);
			SeedAccountDAO.getInstance().updateAccountStatus(id);
			ProxyDataDAO.getInsatnce().updateProxyStatus(proxySettings.getHost());
		}catch(Exception ex) {}
	//	driver.quit();
		return 1;
	}
	
	private void gotoSignup() {
		log.debug("gotoSignup() starts");
		driver.get("http://www.aol.com/");
		
		delay(ExpectedConditions.titleContains("AOL.com"));
		driver.findElement(By.name("om_signup")).click();
		delay(ExpectedConditions.titleContains("AOL"));
		log.debug("gotoSignup() ends");
	}
	
	private void fillForm() {
		seedAccount = new SeedAccountDVO();
		seedAccount.setProxy(proxySettings.getHost() + ":"+proxySettings.getPort());
		seedAccount.setProxyserver(proxySettings.getServerTag());
		
		NameDVO name = NameDAO.getInstance().getRandomName();
		
		driver.findElement(By.id("firstName")).sendKeys(name.getFirst());
		seedAccount.setFname(name.getFirst());
		driver.findElement(By.id("lastName")).sendKeys(name.getLast());
		seedAccount.setLname(name.getLast());
		
		String emailId = name.getEmail();
		
		emailId = emailId.toLowerCase();
		driver.findElement(By.id("desiredSN")).sendKeys(emailId);
		
		//driver.findElement(By.id("password")).click();
		threadSleep(5);
		
		WebElement element = null;
		element = driver.findElement(By.id("usernameError")) ;
		element = driver.findElement(By.id("username-suggestions"));
		element = element.findElement(By.id("snSuggest01"));
		emailId =  element.getText();
		element.click();
		
		
		/*try {
			try {
				driver.findElement(By.id("success-icon"));
			}catch(Exception ex) {
				element = driver.findElement(By.id("usernameError")) ;
				element = driver.findElement(By.id("username-suggestions"));
				element = element.findElement(By.id("snSuggest01"));
				element.click();
				emailId =  element.getText();
				
			}
		}catch(Exception ex) {
		}*/
		
		 
		String pass = ResourceLoader.getPassword();
		 	 driver.findElement(By.id("password")).sendKeys(pass);
			 driver.findElement(By.id("verifyPassword")).sendKeys(pass);
			 seedAccount.setUsername(emailId);
			 seedAccount.setPassword(pass);
			 
			 String monthLbl = AccountCreatorUtil.getMonth();
			 selectMonth(monthLbl);
			 seedAccount.setBmonth(monthLbl);
			 int bday = AccountCreatorUtil.getDay();
			 int byear = AccountCreatorUtil.getYear();

			 driver.findElement(By.id("dobDay")).sendKeys(bday+"");
			 seedAccount.setBday(bday);
			 
			 driver.findElement(By.id("dobYear")).sendKeys(byear+"");
			 seedAccount.setByear(byear);
			 String genderLbl = name.getGender();
			 selectGender(genderLbl);
			 seedAccount.setGender(genderLbl);
			 String zip = ResourceLoader.getZip();
			 
			 driver.findElement(By.id("zipCode")).sendKeys(zip);
			 seedAccount.setZip(zip);
			 
			 driver.findElement(By.id("acctSecurityQuestionSelectBoxItArrowContainer")).click();
			 threadSleep(2);
			 
			 element = driver.findElement(By.id("acctSecurityQuestionSelectBoxItOptions"));
			 element = element.findElement(By.id("1"));
			 String secQuestion = element.getText();
			 element.click();
			
			 seedAccount.setSecQuestion(secQuestion);
			 
			 String ans = ResourceLoader.getSchool();
			 if(ans.length() > 32) {
				 ans = ans.substring(0, 30);
			 }
			 driver.findElement(By.id("acctSecurityAnswer")).sendKeys(ans);
			 seedAccount.setSecAnswer(ans);
			 
			 String phone = ResourceLoader.getPhoneNumber();
			 phone = phone.replace(" ", "");
			 driver.findElement(By.id("mobileNum")).click();
			 driver.findElement(By.id("mobileNum")).sendKeys(phone);
			 seedAccount.setPhone(phone);
			  
			 driver.findElement(By.id("altEMail")).click();
			 threadSleep(2);
			 try {
				element = driver.findElement(By.id("mobileNumError"));
				element.findElement(By.id("error-icon"));
				phone = ResourceLoader.getPhoneNumber();
				driver.findElement(By.id("mobileNum")).sendKeys(phone);
				seedAccount.setPhone(phone);
			 }catch(Exception ex) {
				 
			 }
			 
			 String revoveryEmail = name.getEmail()+"@"+ResourceLoader.getDomain(); 
			 driver.findElement(By.id("altEMail")).sendKeys(revoveryEmail);
			 seedAccount.setCurrentemail(revoveryEmail);
			 
			 log.debug("ENTER CAPTCHA NOW");
			 driver.findElement(By.id("wordVerify")).click();
			 threadSleep(15);
			 // driver.findElement(By.id("wordVerify")).sendKeys(getCaptcha());
			  
		 
		 
		
		
	}
	private String getCaptcha() {
		String url = driver.findElement(By.id("regImageCaptcha")).getAttribute("src");
		String captcha = new CAPTCHAUtil().solveCAPTCHA(url);
		return captcha;
	}
	
	private void selectGender(String gender) {
		 if("M".equalsIgnoreCase(gender)) {
			 gender = "Male";
		 }else {
			 gender = "Female";
		 }
		 
		 driver.findElement(By.id("genderSelectBoxItArrowContainer")).click();
		 threadSleep(1);
		 List<WebElement> months =  driver.findElement(By.id("genderSelectBoxItOptions")).findElements(By.tagName("a"));
		 for(WebElement month: months) {
				if(gender.equalsIgnoreCase(month.getText())) {
					month.click();
					return;
				}
			}
			 
	}
	
	private void selectMonth(String lable) {
		log.debug("selectMonth() starts");
		WebElement element=  driver.findElement(By.id("dobMonthSelectBoxItArrowContainer"));
		element.click();
		threadSleep(3);
		element = driver.findElement(By.id("dobMonthSelectBoxItOptions"));
		List<WebElement> months = element.findElements(By.tagName("a"));
		
		for(WebElement month: months) {
			if(lable.equalsIgnoreCase(month.getText())) {
				month.click();
				return;
			}
		}
		
		log.debug("selectMonth() ends");
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
