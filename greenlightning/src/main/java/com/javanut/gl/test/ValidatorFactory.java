package com.javanut.gl.test;

import com.javanut.gl.api.HTTPResponseReader;

public interface ValidatorFactory {

	boolean validate(long callInstance, HTTPResponseReader reader);
}
