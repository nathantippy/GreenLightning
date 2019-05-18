package com.javanut.gl.impl;

import com.javanut.gl.impl.schema.MessageSubscription;
import com.javanut.pronghorn.pipe.Pipe;

public class MessageReader extends PayloadReader<MessageSubscription> {

	public MessageReader(Pipe<MessageSubscription> pipe) {
		super(pipe);
	}
	
}
