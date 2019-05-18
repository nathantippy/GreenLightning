package com.javanut.oe.greenlightning.api.server;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponder;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.util.AppendableProxy;

public class RestBehaviorHandoffResponder implements PubSubListener {

	HTTPResponder responder;
	
	public RestBehaviorHandoffResponder(GreenRuntime runtime, AppendableProxy console) {
		
		responder = new HTTPResponder(runtime.newCommandChannel(),256*1024);
				
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		if (responder.readHandoffData(payload)) {
			return responder.respondWith(200, false, HTTPContentTypeDefaults.TXT, (w)->{w.writeUTF("sent by responder");});
		} else {
			return false;
		}
		
	}
	
}
