package com.alacance.webMailAutomation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GmailInboxIPCollector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 
	 
		
		String username = "XXXX";
		 String password ="XXXX";
		 String proxyServer ="X.X.x.x:20047";
		 
		 FirefoxDriver driver = null;
		 FirefoxProfile profile = new FirefoxProfile();
		 Proxy proxy = new Proxy();
		 proxy.setHttpProxy(proxyServer);
		 proxy.setFtpProxy(proxyServer);
		 proxy.setSslProxy(proxyServer);
		// profile.setProxyPreferences(proxy);
		 
		 
		 driver = new FirefoxDriver(profile);
		 
		 driver.get("https://mail.google.com/mail/?tab=mm");
			
		 WebElement query = driver.findElement(By.id("Email"));
		 query.sendKeys(username);
		 query = driver.findElement(By.id("Passwd"));
		 query.sendKeys(password);
		 query = driver.findElement(By.id("signIn"));
		 query.click();
		 System.out.println("Wating");
		 new WebDriverWait(driver, 80).until(ExpectedConditions.titleContains("Inbox"));
		 System.out.println("Wait Over");
		 
		 List<WebElement> elements = driver.findElements(By.cssSelector("table.F.cf.zt"));
			for(WebElement element : elements) {
				if(element.isDisplayed()) {
					List<WebElement> mailRows = element.findElements(By.tagName("tr"));
					for(WebElement mailRow : mailRows) {
						List<WebElement> tableCells = mailRow.findElements(By.tagName("td"));
						WebElement fromCell = tableCells.get(4);
						WebElement span = fromCell.findElement(By.tagName("span"));
						String email = span.getAttribute("email");
						WebElement subjectCell = tableCells.get(5);
						span = subjectCell.findElement(By.tagName("span"));
						if(span.getAttribute("innerHTML").contains("<b>")) {
							String parts[] = email.split("@");
							String domain = parts[1];
							String ip = getIP(domain);
							System.out.println(ip+","+domain);
						}
						
					}
					
					
				}
			}
		 
	}
	
	public static String getIP(String domain) {
		String inputLine = null;
		try {
			URL google = new URL("dbsvr/is.php?d="+domain);
			URLConnection yc = google.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(yc
					.getInputStream()));
			String s = null;
			while ((s = in.readLine()) != null) {
				//System.out.println(inputLine);
				inputLine = s;
			}
			in.close();
		}catch(Exception ex) {
			
		}
		return inputLine;
		
	}

}
