package com.alacance.webMailAutomation.test;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.alacance.webMailAutomation.dao.NameDAO;

public class TestNameDAO {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		Properties props = new Properties();
		props.load(new FileInputStream("conf/log4j.properties"));
		PropertyConfigurator.configure(props);
		
			System.out.println(NameDAO.getInstance().getRandomName());
	}

}
