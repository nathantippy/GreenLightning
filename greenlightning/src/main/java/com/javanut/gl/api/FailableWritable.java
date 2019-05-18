package com.javanut.gl.api;

import com.javanut.pronghorn.pipe.ChannelWriter;

public interface FailableWritable {

	FailableWritable NO_OP = new FailableWritable() {
		@Override
		public FailableWrite write(ChannelWriter writer) {
			return FailableWrite.Success;
		}
	};

	FailableWrite write(ChannelWriter writer); //returns true if we have more data to write.
}
