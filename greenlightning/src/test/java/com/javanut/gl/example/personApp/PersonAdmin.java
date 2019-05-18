package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.RestListener;
import com.javanut.pronghorn.pipe.StructuredReader;
import com.javanut.pronghorn.pipe.StructuredWriter;

public class PersonAdmin implements RestListener {

	private PubSubService pubService;

	public PersonAdmin(GreenRuntime runtime) {
		pubService = runtime.newCommandChannel().newPubSubService();
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.isVerbPost()) {
			
			return pubService.publishTopic(GreenStructInternal.adminPersons.name()+"add", 
					w->{
						
						long con = request.getConnectionId();
						long seq = request.getSequenceCode();
						
						StructuredWriter output = w.structured();
												
						output.writeLong(GreenField.connectionId, con);
						output.writeLong(GreenField.sequenceId, seq);
						
						StructuredReader input = request.structured();
												
						input.readLong(GreenField.id, output);
						input.readInt(GreenField.age, output);
						input.readText(GreenField.firstName, output);
						input.readText(GreenField.lastName, output);
						input.readBoolean(GreenField.enabled, output);
						
						output.selectStruct(GreenStructInternal.adminPersons);				
						
					});
			
		} else {
			return pubService.publishTopic(GreenStructInternal.adminPersons.name()+"dump");
		}		
		
		
	}

}
