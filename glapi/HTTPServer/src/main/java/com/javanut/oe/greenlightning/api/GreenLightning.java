package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;

public class GreenLightning {

	public static void main(String[] args) {
		GreenRuntime.run(new HTTPServer(8088,System.out, 8098, true));
	}
	
}
