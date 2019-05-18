package com.javanut.gl.impl.stage;

import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.ChannelWriter;

public interface IngressConverter {

	void convertData(ChannelReader inputStream, ChannelWriter outputStream);
	
}
