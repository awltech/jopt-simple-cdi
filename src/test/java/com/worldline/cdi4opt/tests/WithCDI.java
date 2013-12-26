package com.worldline.cdi4opt.tests;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.worldline.cdi4jopt.annotations.JOptArgument;
import com.worldline.cdi4jopt.annotations.JOptOptions;
import com.worldline.cdi4jopt.annotations.JOptParser;
import com.worldline.cdi4jopt.core.JOptInjector;

public class WithCDI {

	@JOptArgument(name = "value1", required = true)
	private String value1;

	@JOptArgument(name = "value2", required = true)
	private String value2;

	@JOptParser
	private OptionParser optionParser = new OptionParser();

	@JOptOptions
	private OptionSet optionSet = null;

	private JOptInjector injector = new JOptInjector(this);

	public void configureParser() {
		// Does nothing !
	}

	public void parse() {
		this.optionSet = optionParser.parse(new String[] { "-value1", "value1", "-value2", "value2" });
	}

	public void process() {
		injector.inject();
		System.out.println(this.value1);
		System.out.println(this.value2);
	}

	public static void main(String[] args) {
		WithCDI withoutCDI = new WithCDI();
		withoutCDI.configureParser();
		withoutCDI.parse();
		withoutCDI.process();

	}

}
