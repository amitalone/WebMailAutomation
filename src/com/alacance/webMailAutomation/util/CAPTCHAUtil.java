package com.alacance.webMailAutomation.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.DeathByCaptcha.Captcha;
import com.DeathByCaptcha.Client;
import com.DeathByCaptcha.SocketClient;

public class CAPTCHAUtil {
	Logger log = Logger.getLogger(CAPTCHAUtil.class);
	public String solveCAPTCHA(String imageURL) {
		String solved = null;
		try {
			 Client client = (Client)(new SocketClient("amitalone", "Newuser@123"));
		     client.isVerbose = false;
		     if(client.getBalance() == 0) {
		    	 log.info("CAPTACHA SERVICE OUT OF BALANCE");
		     }
		     Captcha captcha = null;
		     URL url = new URL(imageURL);
	          
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedImage img = ImageIO.read(url);
             ImageIO.write( img, "jpg", baos );
             baos.flush();
             byte[] imageInByte = baos.toByteArray();
             baos.close();
             log.info("Requesting CAPTCHA service");
             captcha = client.decode(imageInByte, 120);
             if (null != captcha) {
            	 solved = captcha.text;
            	 log.info("CAPTCHA Solved.");
             }
		}catch (Exception ex) {
			log.error("CAPTCHA Service failed.");
		}
		return solved;
	}
}
