package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.StartupListener;
import com.javanut.pronghorn.util.AppendableProxy;

public class KickoffBehavior implements StartupListener{

	final PubSubFixedTopicService channel0;
	final AppendableProxy target;
	
	public KickoffBehavior(GreenRuntime runtime, Appendable target) {
		
		this.channel0 = runtime.newCommandChannel().newPubSubService("Next");
		this.target = new AppendableProxy(target);
	
	}

	@Override
	public void startup() {
		
		target.append("Your lucky numbers are ...\n");

		channel0.publishTopic();
	}

}
