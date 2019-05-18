package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.GreenRuntime;

public class GreenLightning {

	public static void main(String[] args) {
		GreenRuntime.run(new MyDBMicroservice(true,1443,true),args);
	}
	
}
