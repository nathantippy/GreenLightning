package com.javanut.gl.impl.stage;

import com.javanut.gl.api.HTTPResponseReader;

public interface CallableStaticHTTPResponse<T> {
	
	boolean responseHTTP(T that, HTTPResponseReader reader);

}
