package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.GreenRuntime;

public class GreenLightning {

	/**
	 * Default main entry point for GreenLighting apps, this is generated by Archetype.
	 */
	public static void main(String[] args) {
		//some arguments are show
		GreenRuntime.run(new MyMicroservice(true,1443,false),args);
	}
	
}
