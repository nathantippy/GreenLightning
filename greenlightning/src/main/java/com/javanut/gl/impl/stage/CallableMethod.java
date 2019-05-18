package com.javanut.gl.impl.stage;

import com.javanut.pronghorn.pipe.ChannelReader;

public interface CallableMethod {
	
	boolean method(CharSequence title, ChannelReader reader);
	
}
