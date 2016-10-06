package com.test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AolPlay {

	/**
	 * @param args
	 */
	static WebDriver driver;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		WebDriver driver = new FirefoxDriver();
		driver.get("http://mail.aol.com");
		delay(ExpectedConditions.titleContains("AOL Mail:"));
		
		WebElement query = driver.findElement(By.id("lgnId1"));
		query.sendKeys("dujocalibyk@aol.com");
		query = driver.findElement(By.id("pwdId1"));
		query.sendKeys("vnOutwhmbP");
		query = driver.findElement(By.id("submitID"));
		query.click();
		delay(ExpectedConditions.titleContains("AOL"));
		
	}
	
	private static void delay(ExpectedCondition<?> condition) {
		WebDriverWait wait = new WebDriverWait(driver, 60);
		wait.until(condition);
	}

}
