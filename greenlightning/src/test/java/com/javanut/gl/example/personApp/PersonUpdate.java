package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.RestListener;
import com.javanut.pronghorn.pipe.StructuredWriter;

public class PersonUpdate implements RestListener {

	private final PubSubService pubService;

	public PersonUpdate(GreenRuntime runtime) {
		pubService = runtime.newCommandChannel().newPubSubService();
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		return pubService.publishTopic(GreenStructInternal.modifyPersonState.name(),
				                (w)->{
				                	
				                	StructuredWriter structured = w.structured();
									
				                	structured.writeLong(GreenField.connectionId,
											   request.getConnectionId());
									
									structured.writeLong(GreenField.sequenceId, 
											   request.getSequenceCode());
									
									request.structured().readLong(GreenField.id, structured);									
									
									structured.writeBoolean(GreenField.enabled, 
											   request.structured().isEqual(GreenField.enabled, "e".getBytes()));
									
									structured.selectStruct(GreenStructInternal.modifyPersonState);
									
				                }
				               );
	}

}
