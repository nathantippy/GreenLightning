package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.ClientHostPortInstance;
import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestService;
import com.javanut.gl.api.HTTPResponseListener;
import com.javanut.gl.api.HTTPResponseReader;
import com.javanut.gl.api.PubSubListener;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.StartupListener;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.StructuredReader;

public class HTTPGetBehaviorSingle implements StartupListener, HTTPResponseListener, PubSubListener {
	
	private ClientHostPortInstance session;
	private final HTTPRequestService clientService;
	private final PubSubService pubSubService;

	int countDown = 4000;
	long reqTime = 0;
	
	public HTTPGetBehaviorSingle(GreenRuntime runtime, ClientHostPortInstance session) {
		this.session = session;
		GreenCommandChannel cmd = runtime.newCommandChannel();
		clientService = cmd.newHTTPClientService();
		pubSubService = cmd.newPubSubService();
		
	}


	@Override
	public void startup() {
		pubSubService.publishTopic("next");
	}

	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {

	//	System.out.println("xxxxxxxxxxxxxxxxxxx");
//		reader.openPayloadData( (r)-> {
//			StructuredReader s = r.structured();
//			int value1 = s.readInt(Fields.AGE);
//			String value2 = s.readText(Fields.NAME);
//			
//			//System.out.println(value1+"  "+value2);
//		});
		
		pubSubService.publishTopic("next");
		
		return true;
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		if (--countDown<=0) {
			clientService.httpGet(session, "/shutdown?key=shutdown");
			pubSubService.publishTopic("shutdown");
		}
		
		reqTime = System.nanoTime();
		return clientService.httpGet(session, "/testPageB");

	}

}
