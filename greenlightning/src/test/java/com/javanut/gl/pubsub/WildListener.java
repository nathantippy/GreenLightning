package com.javanut.gl.pubsub;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class WildListener implements PubSubListener {
	private final AppendableProxy target;
	private final GreenRuntime runtime;

	WildListener(Appendable target, GreenRuntime runtime) {
		this.target = Appendables.proxy(target);
		this.runtime = runtime;
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {		
		target.append(topic).append("\n");
		if (topic.toString().endsWith("shutdown")) {
			runtime.shutdownRuntime();
		}
		return true;
	}
}
