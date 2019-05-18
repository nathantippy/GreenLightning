package com.javanut.gl.api;

import com.javanut.gl.impl.schema.MessagePubSub;
import com.javanut.pronghorn.pipe.DataOutputBlobWriter;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeWriter;
import com.javanut.pronghorn.pipe.token.OperatorMask;
import com.javanut.pronghorn.pipe.token.TokenBuilder;
import com.javanut.pronghorn.pipe.token.TypeMask;

public class PubSubWriter extends PayloadWriter<MessagePubSub> {

	public PubSubWriter(Pipe<MessagePubSub> p) {
		super(p);
	}


}
