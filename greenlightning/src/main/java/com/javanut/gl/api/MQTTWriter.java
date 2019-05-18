package com.javanut.gl.api;

import com.javanut.pronghorn.network.schema.MQTTClientRequestSchema;
import com.javanut.pronghorn.pipe.Pipe;

public class MQTTWriter extends PayloadWriter<MQTTClientRequestSchema> {

	public MQTTWriter(Pipe<MQTTClientRequestSchema> p) {
		super(p);
	}

}
