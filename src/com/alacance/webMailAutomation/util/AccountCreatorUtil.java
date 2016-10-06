package com.alacance.webMailAutomation.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountCreatorUtil {

	static final List<String> months = new ArrayList<String>(Arrays.asList(new String[]{"January","February", "March", "April", "May",
		"June", "July", "August", "September", "October", "November", "December"}));
	
	public static String getMonth() {
		Collections.shuffle(months);
		return months.get(0);
	}
	
	public static int getYear() {
		return RangeRandom.next(1968, 1992);
	}
	
	public static int getDay() {
		return RangeRandom.next(1, 28);
	}
	

}
