package com.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ServerLoft {
	public static void main(String[] args) {
		
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setPreference("browser.download.folderList",2);
		firefoxProfile.setPreference("browser.download.manager.showWhenStarting",false);
		firefoxProfile.setPreference("browser.download.dir","c:\\Downloads\\in");
		firefoxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","text/pdf,message/rfc822,text/html");
		
		WebDriver driver = new FirefoxDriver(firefoxProfile);
		
		System.out.println("Loaded, gettig");
		driver.get("https://my.serverloft.com/en/Generic/Auth/Index/login");
		System.out.println("Loaded");
		WebElement query = driver.findElement(By.name("username"));
		query.sendKeys("pkale1");
		query = driver.findElement(By.name("password"));
		query.sendKeys("amitalone123");
		
		query = driver.findElement(By.className("button"));
		query.click();
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(ExpectedConditions.titleContains("Dashboard"));
		
		driver.get("https://my.serverloft.com/en/Customer/Invoice/Index");
		
		
	}
}
