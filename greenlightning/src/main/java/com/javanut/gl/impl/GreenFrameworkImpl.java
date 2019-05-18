package com.javanut.gl.impl;

import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.stage.scheduling.GraphManager;

public class GreenFrameworkImpl extends BuilderImpl<GreenRuntime> implements GreenFramework {

	public GreenFrameworkImpl(GraphManager gm, String[] args) {
		super(gm, args);
	}

}
