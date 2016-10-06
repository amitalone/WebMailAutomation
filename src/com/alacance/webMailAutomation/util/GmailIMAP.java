package com.alacance.webMailAutomation.util;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import com.sun.mail.imap.IMAPFolder;

public class GmailIMAP {

	public String readVerificationCode(String userName, String password,String forUser) throws Exception {
		 String code = null;
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps");

		Session session;

		session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("imaps");
		store.connect("imap.gmail.com", userName, password);
		
		IMAPFolder folder = (IMAPFolder) store.getFolder("inbox");
	    folder.open(Folder.READ_WRITE);

	    Flags seen = new Flags(Flags.Flag.SEEN);
	    FlagTerm unseenFlagTerm = new FlagTerm(seen,false);
	    Message messages[] = folder.search(unseenFlagTerm); 
	   
	    for(Message message : messages) 
        {
	    	String values[] = message.getHeader("From");
	    	 for(String v : values) {
	    		 if(v.contains("account-security-noreply@account.microsoft.com")) {
	        			code = getVerificationCode(message, forUser);
	        			if(null != code ) {
	        				break;
	        			}
	        			 
	        		}
	    	 }
        }
		return code;
	}
	
	private String getVerificationCode(Message message, String forUser) throws Exception {
		String content = message.getContent()+"";
		if(content.contains(forUser)) {
			String lines[] = content.split("\n");
			for(String line : lines) {
				if(line.contains("code:")) {
					line = line.replace("Here is your code:", "");
					return line.trim();
				}
			}
		}
		return null;
	}
}
