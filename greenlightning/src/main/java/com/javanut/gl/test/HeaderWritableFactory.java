package com.javanut.gl.test;

import com.javanut.pronghorn.network.http.HeaderWritable;

public interface HeaderWritableFactory {

	HeaderWritable headerWritable(long callInstance);
}
