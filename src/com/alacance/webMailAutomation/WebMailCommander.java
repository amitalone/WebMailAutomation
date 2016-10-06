package com.alacance.webMailAutomation;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class WebMailCommander {
	  @Parameter
	  private List<String> parameters = new ArrayList<String>();
	 
	  @Parameter(names = { "-isp"}, description = "Email service provide, eg gmail, hotmail")
	  public String isp;
	 
	  @Parameter(names = "-accounts", description = "account file.")
	  public String accounts;
	 
	  @Parameter(names = "-debug", description = "Debug mode")
	  public boolean debug = false;
	  
	  @Parameter(names = "-help", description = "Display help")
	  public String help;
	  
	  @Parameter(names = "-mode", description = "Mode help")
	  public String mode;
}
