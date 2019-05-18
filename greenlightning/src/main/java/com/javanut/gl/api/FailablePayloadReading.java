package com.javanut.gl.api;

import com.javanut.pronghorn.pipe.ChannelReader;

public interface FailablePayloadReading {

	boolean read(ChannelReader reader);

}
