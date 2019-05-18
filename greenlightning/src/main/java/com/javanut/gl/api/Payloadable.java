package com.javanut.gl.api;

import com.javanut.pronghorn.pipe.ChannelReader;

public interface Payloadable {

	void read(ChannelReader reader);

}
