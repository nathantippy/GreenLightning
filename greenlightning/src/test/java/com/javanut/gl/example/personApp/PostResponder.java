package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelReader;

public class PostResponder implements PubSubListener {

	private final HTTPResponseService resp;

	public PostResponder(GreenRuntime runtime) {
		resp = runtime.newCommandChannel().newHTTPResponseService();
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		return resp.publishHTTPResponse(
                 payload.structured().readLong(GreenField.connectionId),
				 payload.structured().readLong(GreenField.sequenceId), 
				 payload.structured().readInt(GreenField.status),
				 false,
				 HTTPContentTypeDefaults.JSON,
				 w-> {
					 
					ChannelReader reader = payload.structured().read(GreenField.payload);
					reader.readInto(w, reader.available());
														 
				 });
		
	}

}
