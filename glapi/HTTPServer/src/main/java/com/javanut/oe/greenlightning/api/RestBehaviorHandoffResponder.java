package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponder;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.util.AppendableProxy;

public class RestBehaviorHandoffResponder implements PubSubListener {

	HTTPResponder responder;
	
	public RestBehaviorHandoffResponder(GreenRuntime runtime, AppendableProxy console) {
		
		responder = new HTTPResponder(runtime.newCommandChannel(), 256*1024);
				
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		boolean result = responder.readHandoffData(payload);
		if (result) {
			responder.respondWith(200, false, HTTPContentTypeDefaults.TXT, (w)->{w.writeUTF8Text("sent by responder");});
		}
		
		return result;
	}
	
}
