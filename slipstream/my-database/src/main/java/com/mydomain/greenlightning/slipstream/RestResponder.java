package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.StructuredReader;

public class RestResponder implements PubSubListener {

	private HTTPResponseService responseService;

	public RestResponder(GreenRuntime runtime) {		
		responseService = runtime.newCommandChannel().newHTTPResponseService(4,400);		
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		StructuredReader struct = payload.structured();
		return responseService.publishHTTPResponse(
				struct.readLong(Field.CONNECTION),
				struct.readLong(Field.SEQUENCE),
				struct.readInt(Field.STATUS),
				HTTPContentTypeDefaults.JSON, w->{
					struct.readText(Field.PAYLOAD, w);
				});
	}
	
}
