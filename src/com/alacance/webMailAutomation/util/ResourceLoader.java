package com.alacance.webMailAutomation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

import com.alacance.webMailAutomation.ProxySettings;
import com.alacance.webMailAutomation.UserAccount;

public class ResourceLoader {

	private static HashMap<String, String> _routine = new HashMap<String, String>();
	private static HashMap<String, String> _configs = new HashMap<String, String>();
	private static List<UserAccount> _accounts = new ArrayList<UserAccount>();
	private static List<String> _phoneBook = new ArrayList<String>();
	public static String accountPath = null;
	private final static Logger log = Logger.getLogger(ResourceLoader.class);
	private static List<String> _suffixes = new ArrayList<String>();
	private static List<String> _domains = new ArrayList<String>();
	private static List<String> _passwords = new ArrayList<String>();
	private static List<String> _zip = new ArrayList<String>();
	private static List<String> _schools = new ArrayList<String>();
	private static List<UserAccount> _hmVerificationIds = new ArrayList<UserAccount>();
	
	static {
		init();
	}
	
	
	
	public static String getServerName() {
		return _configs.get("ServerName");
	}
	public static List<UserAccount> getAccounts() {
		Collections.shuffle(_accounts);
		return _accounts;
	}
	
	private static void init() {
		initConfig();
	}
	
	
	public static String getPhoneNumber() {
		if(!_phoneBook.isEmpty()) {
			Collections.shuffle(_phoneBook);
				return _phoneBook.get(0);
		}
		return null;
	}
	
	public static String getZip() {
		if(!_zip.isEmpty()) {
			Collections.shuffle(_zip);
				return _zip.get(0);
		}
		return "07307";
	}
	
	public static String getSchool() {
		if(!_schools.isEmpty()) {
			Collections.shuffle(_schools);
				return _schools.get(0);
		}
		return "NESH";
	}
	
	public static String getConfigValue(String key) {
		return _configs.get(key);
	}
	
	public static int getThreadPoolSize() {
		return Integer.parseInt(_configs.get("ThreadPoolCount"));
	}
	public static boolean clickBodyLink() {
		String value = _configs.get("clickmsgbodylink");
		return "true".equalsIgnoreCase(value);
	}
	public static String getMYSQLConnectionString() {
		return _configs.get("mysqlconnectionString");
	}
	public static String getDBUser() {
		return _configs.get("dbuser");
	}
	public static String getDBPassword() {
		return _configs.get("dbpass");
	}
	
	public static boolean processMails() {
		boolean emptyAndProcess = false;
		try {
			if(_configs.get("hotmail.process").equalsIgnoreCase("true")) {
				emptyAndProcess = true;
			}else {
				emptyAndProcess = false;
			}
		}catch(Exception ex) {
		}
		return emptyAndProcess;
	}
	
	public static boolean emptyGmailInbox() {
		boolean flag = false;
		try {
			if(_configs.get("gmai.emptyInbox").equalsIgnoreCase("true")) {
				flag = true;
			}else {
				flag = false;
			}
		}catch(Exception ex) {
		}
		return flag;
	}
	
	public static boolean emptyJunk() {
		boolean flag = false;
		try {
			if(_configs.get("hotmail.emptyjunk").equalsIgnoreCase("true")) {
				flag = true;
			}else {
				flag = false;
			}
		}catch(Exception ex) {
		}
		return flag;
	}
	
	
	public static boolean emptyInbox() {
		boolean flag = false;
		try {
			if(_configs.get("hotmail.emptyinbox").equalsIgnoreCase("true")) {
				flag = true;
			}else {
				flag = false;
			}
		}catch(Exception ex) {
		}
		return flag;
	}
	
	public static void initPhoneBook() {
		log.debug("initConfig() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/phonebook"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String ph = null;
			String name = null;
			while ((strLine = br.readLine()) != null) {
				String parts[] = strLine.split(",");
				ph = parts[0];
				name = parts[1];
				_phoneBook.add(ph);
			}
			log.debug("Config Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initConfig() ends");
	}
	
	public static void initSuffixes() {
		log.debug("initConfig() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/suffix"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				_suffixes.add(strLine);
			}
			log.debug("Config Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initConfig() ends");
	}

	public static String getSuffix() {
		if(!_suffixes.isEmpty()) {
			Collections.shuffle(_suffixes);
			return _suffixes.get(0);
		}
		return "M2";
	}
	
	public static void initHMVerificationIds() {
		log.debug("initHMVerificationIds() starts");
		BufferedReader br = null;
	 
		try {
			InputStream in = new FileInputStream(new File("conf/hmverificationids"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String values[] = strLine.split(",");
				UserAccount acc = new UserAccount();
				acc.setUserName(values[0]);
				acc.setPassword(values[1]);
				_hmVerificationIds.add(acc);
			}
			log.debug("initHMVerificationIds Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initHMVerificationIds() ends");
	 
	}

	
	public static void initDomains() {
		log.debug("initConfig() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/domains"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				_domains.add(strLine);
			}
			log.debug("Config Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initConfig() ends");
	}


	public static void initSchool() {
		log.debug("initSchool() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/schools"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				_schools.add(strLine);
			}
			log.debug("Schools Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initSchool() ends");
	}
	
	public static void iniZip() {
		log.debug("iniZip() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/zip"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				_zip.add(strLine);
			}
			log.debug("Zip Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("iniZip() ends");
	}
	
	public static String getDomain() {
		if(!_domains.isEmpty()) {
			Collections.shuffle(_domains);
			return _domains.get(0);
		}
		return "hotmail.com";
	}
	
	public static void initPasswords() {
		log.debug("initConfig() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/passdb"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				_passwords.add(strLine);
			}
			log.debug("Config Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initConfig() ends");
	}

	public static String getPassword() {
		if(!_passwords.isEmpty()) {
			Collections.shuffle(_passwords);
			String pass = _passwords.get(0);
			if(pass.length() < 8) {
				pass = pass +"@"+ RangeRandom.next(11111, 99999);
			}
			int r = RangeRandom.next(1, 50);
			
			if(r > 30) {
				pass = RangeRandom.next(100, 1000) + "#" + pass;
			}
			return pass;		
		}
		return "Newuser@123";
	}
	
	public static List<String> getGmailMultiMailIdentifire() {
		String line = getConfigValue("gmail.multiMailIdentifire");
		String vals[] = line.split(",");
		return new ArrayList<String>(Arrays.asList(vals));
	}
	
	public static List<String> getAOLMultiMailIdentifire() {
		String line = getConfigValue("aol.multiMailIdentifire");
		String vals[] = line.split(",");
		return new ArrayList<String>(Arrays.asList(vals));
	}
	
	private static void initConfig() {
		log.debug("initConfig() starts");
		BufferedReader br = null;
		try {
			InputStream in = new FileInputStream(new File("conf/config"));
			//InputStream in =  ResourceLoader.class.getResourceAsStream("/config");
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			String key = null;
			String value = null;
			while ((strLine = br.readLine()) != null) {
				String parts[] = strLine.split("=");
				key = parts[0];
				value = parts[1];
				key = key.trim();
				value = value.trim();
				_configs.put(key, value);
			}
			log.debug("Config Loaded.");
			br.close();
		}catch (Exception e) {
			 e.printStackTrace();
		} 
		log.debug("initConfig() ends");
	}
	
	public static int gmailDelaybeforeDriverClose() {
		try {
			String result = _configs.get("gmail.delaybeforeclose"); 
			 return Integer.parseInt(result);
			 
		}catch(Exception ex) {
			return 0;
		}
	}
	
	public static boolean gmailLoginOnly() {
		try {
			String result = _configs.get("gmail.loginonly"); 
			if("true".equalsIgnoreCase(result)) {
				return true;
			}
			return false;
		}catch(Exception ex) {
			return false;
		}
	}

	public static boolean closeDriver() {
		try {
			String result = _configs.get("closedriver"); 
			if("false".equalsIgnoreCase(result)) {
				return false;
			}
			return true;
		}catch(Exception ex) {
			return true;
		}
	}
	public static void initAccounts() {
		log.debug("initAccounts() starts.");
	try {
		
		if(null == accountPath) {
			accountPath = "conf/accounts.csv";
		}
		CSVReader reader = new CSVReader(new FileReader(accountPath));
		String [] nextLine;
		reader.readNext();
		String username = null;
		String password = null;
		String proxy = null;
		String recoveryEmail = null;
		String recoveryPhone =null;
		String server = null;
		String port  = null;
		UserAccount account = null;
		 
	    while ((nextLine = reader.readNext()) != null) {
	    	username = nextLine[2];
	    	password = nextLine[3];
	    	try {
	    	proxy = nextLine[4];
	    	recoveryEmail =  nextLine[5];
	    	recoveryPhone =  nextLine[6];
	    	}catch(ArrayIndexOutOfBoundsException aie) {
	    		
	    	}
	    	if(StringUtils.isEmpty(recoveryPhone)) {
	    		recoveryPhone = null;
	    	}
	    	if(StringUtils.isEmpty(recoveryEmail)) {
	    		recoveryEmail = null;
	    	}
	    	try {
				String ppart[] = proxy.split(":");
				server =  ppart[0];
				port = ppart[1];
			}catch(IndexOutOfBoundsException ibx) {
				proxy = null;
			}
			account = new UserAccount();
			account.setUserName(username);
			account.setPassword(password);
			account.setRecoveryPhone(recoveryPhone);
			account.setRecoveryEmail(recoveryEmail);
			
			if(null != proxy) {
				ProxySettings proxySettings = new ProxySettings(server, port);
				proxySettings.setServerTag(nextLine[7]);
				account.setProxySettings(proxySettings);
			}else {
				account.setProxySettings(null);
			}
			
			_accounts.add(account);
			log.debug(account);
	    }
	    reader.close();
		log.debug("accounts loaded");
	}catch(Exception ex) {
		System.out.println("Accounts could not be loaded.");
		ex.printStackTrace();
	    System.exit(0);
	}
	log.debug("initAccounts() ends.");
 }
	
	public static UserAccount getVerificationId() {
		Collections.shuffle(_hmVerificationIds);
		return _hmVerificationIds.get(0);
	}
	
/*	public static String getVerificationEmailID() {
		return getConfigValue("hotmail.verificationID");
	}
	
	public static String getVerificationIDPassword() {
		return getConfigValue("hotmail.verificationIDPassword");
	}*/
	
}
