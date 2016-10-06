package com.test;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SingleHopper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		/////
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setPreference("browser.download.folderList",2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting",false);
		firefoxProfile.setPreference("browser.download.dir","c:\\Downloads\\eml");
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","text/csv,message/rfc822,text/html");
		 
		
		WebDriver driver = new FirefoxDriver(firefoxProfile);
		
		System.out.println("Loaded, gettig");
		driver.get("https://leap3.singlehop.com/");
		System.out.println("Loaded");
		WebElement query = driver.findElement(By.name("username"));
		query.sendKeys("178948");
		query = driver.findElement(By.name("password"));
		query.sendKeys("Muffin07");
		
		query = driver.findElement(By.name("login"));
		query.click();
		
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.titleContains("Dashboard"));
		
		
		//https://leap3.singlehop.com/support/home/spam-notifications/507/
		for(int pcount=318; pcount<=507; pcount++) {
			System.out.println("In Page Loop");
			String currentURL = "https://leap3.singlehop.com/support/home/spam-notifications/"+pcount+"/";
			driver.get(currentURL);
			
			System.out.println(currentURL);
			
			try {
				wait.until(ExpectedConditions.titleContains("Support"));
			}catch (TimeoutException e) {
				driver.navigate().refresh();
				wait.until(ExpectedConditions.titleContains("Support"));
			}
			
			List<WebElement> elements = driver.findElements(By.partialLinkText("Email Feedback Report for"));
			
			List<String> links = new ArrayList<String>();
			for(WebElement element : elements) {
				String link = element.getAttribute("href");
				links.add(link);
			}
			
			int i=0;
			for(String link: links) {
				i++;
				try {
					saveEML(driver, link);
				}catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("SAVED "+i+ " Message from current page");
				
			}
		}


	}

	private static void saveEML(WebDriver driver, String link) throws Exception {
		driver.navigate().to(link);
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.titleContains("Support"));
		
		
		List<WebElement> attachments =	driver.findElements(By.partialLinkText("attachment-2"));
		if(attachments.size() > 0) {
			driver.get(attachments.get(0).getAttribute("href"));
			try {
				System.out.println("wating");
				Thread.currentThread().wait(100);
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}
	
	 
}
