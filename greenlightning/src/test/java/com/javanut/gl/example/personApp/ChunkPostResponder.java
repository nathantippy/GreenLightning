package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.PubSubListener;
import com.javanut.gl.api.PubSubMethodListener;
import com.javanut.pronghorn.network.config.HTTPContentType;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelReader;

public class ChunkPostResponder implements PubSubMethodListener {

	private final HTTPResponseService resp;

	public ChunkPostResponder(GreenRuntime runtime) {
		resp = runtime.newCommandChannel().newHTTPResponseService();
	}

	public boolean beginChunks(CharSequence topic, ChannelReader payload) {
						
		return resp.publishHTTPResponse(
				                 payload.structured().readLong(GreenField.connectionId),
								 payload.structured().readLong(GreenField.sequenceId), 
								 payload.structured().readInt(GreenField.status),
								 payload.structured().readBoolean(GreenField.hasContinuation),
								 HTTPContentTypeDefaults.JSON,
								 w-> {
									 
									ChannelReader reader = payload.structured().read(GreenField.payload);
									reader.readInto(w, reader.available());
																		 
								 });
	}

	public boolean continueChunks(CharSequence topic, ChannelReader payload) {
		
		return resp.publishHTTPResponseContinuation(
				 payload.structured().readLong(GreenField.connectionId),
				 payload.structured().readLong(GreenField.sequenceId),
				 payload.structured().readBoolean(GreenField.hasContinuation),
				 w-> {
					 
					ChannelReader reader = payload.structured().read(GreenField.payload);
					reader.readInto(w, reader.available());
														 
				 });
	}
	
}
