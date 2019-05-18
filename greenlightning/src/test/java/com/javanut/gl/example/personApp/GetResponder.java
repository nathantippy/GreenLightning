package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.pipe.ChannelReader;

public class GetResponder implements PubSubListener {

	private final HTTPResponseService resp;

	public GetResponder(GreenRuntime runtime) {
		resp = runtime.newCommandChannel().newHTTPResponseService();
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		return resp.publishHTTPResponse(payload.structured().readLong(GreenField.connectionId), 
									    payload.structured().readLong(GreenField.sequenceId), 
									    payload.structured().readInt(GreenField.status));
		
	}

}
