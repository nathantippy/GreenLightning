package com.javanut.oe.greenlightning.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;

public class ShutdownRestListener implements RestListener{

	private HTTPResponseService responseService;
	private GreenCommandChannel newCommandChannel;

	private final byte[] pass = "shutdown".getBytes();
	private static final Logger logger = LoggerFactory.getLogger(ShutdownRestListener.class);
	
	public ShutdownRestListener(GreenRuntime runtime) {
		this.newCommandChannel = runtime.newCommandChannel();
		this.responseService = newCommandChannel.newHTTPResponseService();		
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.structured().isEqual(Field.KEY, pass)) {
			
			if (!responseService.hasRoomFor(2)) {//reponse then shutdown
				return false;
			}
			
			if (responseService.publishHTTPResponse(request, 200)) {		
				
				responseService.requestShutdown();
				
				return true;
			} 
			return false;
		} else {
			if (responseService.publishHTTPResponse(request, 404)) {	
				return true;
			} 
			return false;
		}
	}

}
