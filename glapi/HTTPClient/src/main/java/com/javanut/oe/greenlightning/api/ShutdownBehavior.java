package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.pipe.ChannelReader;

public class ShutdownBehavior implements PubSubListener {

	private final GreenRuntime runtime;
	public ShutdownBehavior(GreenRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		runtime.shutdownRuntime();
		
		return true;
	}

}
