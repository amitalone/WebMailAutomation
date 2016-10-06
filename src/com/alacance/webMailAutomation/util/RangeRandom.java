package com.alacance.webMailAutomation.util;

import java.util.Random;

public class RangeRandom {


	public static int next(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}
}
