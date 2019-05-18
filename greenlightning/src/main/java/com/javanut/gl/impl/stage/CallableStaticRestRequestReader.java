package com.javanut.gl.impl.stage;

import com.javanut.gl.api.HTTPRequestReader;

public interface CallableStaticRestRequestReader<T> {

	boolean restRequest(T that, HTTPRequestReader request);
	
}
