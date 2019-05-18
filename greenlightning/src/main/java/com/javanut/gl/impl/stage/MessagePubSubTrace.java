package com.javanut.gl.impl.stage;

import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeReader;
import com.javanut.pronghorn.util.Appendables;

public class MessagePubSubTrace {

	private Pipe<?> pipe;
	private int topic;
	private int payload;
	
	public void init(Pipe<?> pipe, int topicLOC, int payloadLOC) {
		this.pipe = pipe;
		this.topic = topicLOC;
		this.payload = payloadLOC;
	}

	/**
	 *
	 * @return builder.toString
	 */
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("topic: ");
		PipeReader.readUTF8(pipe, topic, builder);
		builder.append(" payload: ");
		
		DataInputBlobReader<?> stream = PipeReader.inputStream(pipe, payload);
		
		while (stream.hasRemainingBytes()) {
			
			Appendables.appendFixedHexDigits(builder, (0xFF&stream.readByte()), 8);
			if (stream.hasRemainingBytes()) {
				builder.append(',');
			}
		}
		
		return builder.toString();
	}

}
