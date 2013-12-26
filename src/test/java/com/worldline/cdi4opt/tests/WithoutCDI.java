package com.worldline.cdi4opt.tests;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class WithoutCDI {

	private String value1;
	
	private String value2;
	
	private OptionParser optionParser = new OptionParser();
	
	private OptionSet optionSet;
	
	public void configureParser() {
		optionParser.allowsUnrecognizedOptions();
		optionParser.accepts("value1").withRequiredArg().ofType(String.class).describedAs("value1");
		optionParser.accepts("value2").withRequiredArg().ofType(String.class).describedAs("value2");
	}
	
	public void parse() {
		this.optionSet = optionParser.parse(new String[] {"-value1", "value1", "-value2", "value2"});
	}
	
	public void process() {
		this.value1 = optionSet.has("value1") ? (String) optionSet.valueOf("value1") : null;
		this.value2 = optionSet.has("value2") ? (String) optionSet.valueOf("value2") : null;
		System.out.println(this.value1);
		System.out.println(this.value2);
	}
	
	public static void main(String[] args) {
		WithoutCDI withoutCDI = new WithoutCDI();
		withoutCDI.configureParser();
		withoutCDI.parse();
		withoutCDI.process();
		
		
	}

}
