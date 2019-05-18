package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;

public class GreenLightning {

	public static void main(String[] args) {
		GreenRuntime.run(new StateMachine(System.out, 200));
	}
	
}
