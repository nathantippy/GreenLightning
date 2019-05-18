package com.javanut.gl.test;

import com.javanut.pronghorn.pipe.ChannelWriter;

public interface WritableFactory {

	void payloadWriter(long callInstance, ChannelWriter writer);
	
}
