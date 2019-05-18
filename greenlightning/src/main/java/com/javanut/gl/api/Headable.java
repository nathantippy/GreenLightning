package com.javanut.gl.api;

import com.javanut.pronghorn.network.config.HTTPHeader;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.struct.StructFieldVisitor;

public interface Headable extends StructFieldVisitor<HTTPHeader>{

	public void read(HTTPHeader header, ChannelReader reader);

}
