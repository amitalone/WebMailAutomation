package com.alacance.webMailAutomation.util;

import com.alacance.webMailAutomation.UserAccount;

public class AccountIdentifire {

	public static String getType(UserAccount account) {
		if(account.getUserName().contains("msn") || account.getUserName().contains("outlook") || account.getUserName().contains("hotmail")) {
			return "hotmail";
		}
		if(account.getUserName().contains("aol")) {
			return "aol";
		}
		return "none";
	}
}
