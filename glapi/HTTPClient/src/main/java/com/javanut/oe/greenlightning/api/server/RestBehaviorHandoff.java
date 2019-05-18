package com.javanut.oe.greenlightning.api.server;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.RestListener;

public class RestBehaviorHandoff implements RestListener {
		
	private final PubSubFixedTopicService cmd;
    
	public RestBehaviorHandoff(GreenRuntime runtime, String topic) {
		this.cmd = runtime.newCommandChannel().newPubSubService(topic);

	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		return cmd.publishTopic((writer)->{ 
			request.handoff(writer);			
		});
		
	}

}
