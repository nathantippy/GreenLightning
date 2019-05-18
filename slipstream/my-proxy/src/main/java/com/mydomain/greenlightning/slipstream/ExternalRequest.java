package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.ClientHostPortInstance;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPRequestService;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.RestListener;

public class ExternalRequest implements RestListener {

	private final HTTPRequestService clientService;
	private final ClientHostPortInstance session;
	private final PubSubFixedTopicService pubSubService;
	private final String path;

	public ExternalRequest(GreenRuntime runtime, 
			               ClientHostPortInstance session, String path,
			               String topic) {
	
		this.clientService = runtime.newCommandChannel().newHTTPClientService(4,400);
		this.pubSubService = runtime.newCommandChannel().newPubSubService(topic,4,400);
		this.session = session;
		this.path = path;
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		if (pubSubService.hasRoomFor(1)) {
			if (!clientService.httpGet(session, path)) {
				return false;//try again later
			};
			pubSubService.presumePublishTopic(w-> request.handoff(w) );		
			return true;
		} else {
			return false;
		}
	}

}
