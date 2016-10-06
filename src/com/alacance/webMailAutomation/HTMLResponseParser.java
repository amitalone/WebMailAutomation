package com.alacance.webMailAutomation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLResponseParser {
	private static Logger log = Logger.getLogger(HTMLResponseParser.class);
	
	public static String getFolderId(String response, String key) {
		log.debug("Entreing getFolderId");
		Document doc = Jsoup.parse(response);
		Element ul = doc.select("ul#folderListControlUl").first();
		for(Element li : ul.select("li")) {
			try{
				// Class TextSemiBold
				if(li.attr("nm").equalsIgnoreCase(key)) {
					String id = li.attr("id");
					log.debug("Found ID " + id);
					log.debug("Leaving getFolderId");
					return id;
				}
			}catch(NullPointerException nex) {
				log.debug("NullPointerException Occured");
			}
		}
		log.debug("Leaving getFolderId");
		return "NONE";
	}
	
	public static List<String> getUnreadEmailsOnCurrentPage(String pageSource) {
			log.debug("Entreing getUnreadEmailsOnCurrentPage");
			Document doc = Jsoup.parse(pageSource);
			Element inboxDiv =	doc.select("div#messageListContentContainer").first();
			Elements mailRows =	inboxDiv.select("li");
			List<String> unreadEmails = new ArrayList<String>();
			for(Element mailRow : mailRows) {
				try{
				String id = mailRow.attr("id");
				String subject =mailRow.select("span.sb").first().html();
				if(subject.contains("TextSemiBold")) {
					unreadEmails.add(id);
				}
				}catch(NullPointerException nex) {
				}
			}
			log.debug("Leaving getUnreadEmailsOnCurrentPage");
			return unreadEmails;
	}
}
