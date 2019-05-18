package com.javanut.gl.impl.stage;

import com.javanut.pronghorn.pipe.ChannelReader;

public interface CallableStaticMethod<T> {
	
	boolean method(T that, CharSequence title, ChannelReader reader);
	
}
