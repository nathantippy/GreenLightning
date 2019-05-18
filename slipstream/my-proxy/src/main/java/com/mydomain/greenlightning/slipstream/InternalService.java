package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;

public class InternalService implements RestListener {

	private final HTTPResponseService responseService;

	public InternalService(GreenRuntime runtime) {
		responseService = runtime.newCommandChannel().newHTTPResponseService(4,400);		
	}
	
	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		return responseService.publishHTTPResponse(request, 
				                                   200, 
				                                   HTTPContentTypeDefaults.PLAIN,
				                                   (w)-> w.append("Hello World") );
		
	}

}
