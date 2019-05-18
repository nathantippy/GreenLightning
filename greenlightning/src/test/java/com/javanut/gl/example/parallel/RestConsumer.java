package com.javanut.gl.example.parallel;

import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.MsgCommandChannel;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.RestListener;
import com.javanut.gl.api.Writable;
import com.javanut.pronghorn.pipe.ChannelWriter;

public class RestConsumer implements RestListener {
	
	private static final byte[] v2 = "st".getBytes();
	private static final byte[] v1 = "value".getBytes();
	private GreenCommandChannel cmd2;	
	private HTTPRequestReader requestW;
	private final long fieldA;
	private final long fieldB;
	private final Object valueObject;
	
	private Writable w = new Writable() {

		@Override
		public void write(ChannelWriter writer) {
			writer.writePackedLong(requestW.getConnectionId());
			writer.writePackedLong(requestW.getSequenceCode());	
			long track = 0;//unknown
			writer.writePackedLong(track);
		}
		
	};
	private PubSubFixedTopicService messageService;
	private HTTPResponseService responseService;
	public RestConsumer(GreenRuntime runtime, long fieldA, long fieldB,
			Object objectA,
			Object objectB,
			Object valueObj) {		
		this.cmd2 = runtime.newCommandChannel();		
		this.messageService = this.cmd2.newPubSubService("/send/200",64,400);
		this.responseService = this.cmd2.newHTTPResponseService(8,400);
		this.fieldA = fieldA;
		this.fieldB = fieldB;	
		this.valueObject = valueObj;
				
	}


	@Override
	public boolean restRequest(final HTTPRequestReader request) {
		
		if (!( request.isVerbPost() || request.isVerbGet() )) {
			responseService.publishHTTPResponse(request, 404);
		}

		assert(request.structured().isEqual(fieldA, v1));
		
		assert(request.structured().isEqual(valueObject, v2)) : "found "+request.structured().readText(valueObject);
				
		int b = request.structured().readInt(fieldB);
		if (b!=123) {
			throw new UnsupportedOperationException();
		}
		
		requestW = request;
		return messageService.publishTopic(w);

		
	//	cmd2.publishTopic("/test/gobal");//tell the watcher its good

	}

}
