package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.GreenRuntime;

public class GreenLightning {

	public static void main(String[] args) {		
		GreenRuntime.run(new MyProxy(true,8088),args);
	}
	
}
