package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.gl.api.ShutdownListener;

public class ShutdownBehavior implements ShutdownListener, RestListener{

	private final HTTPResponseService channel;
	private final long keyFieldId;
	private final byte[] PASS1 = "2709843294721594".getBytes();
	private final byte[] PASS2 = "A5E8F4D8C1B987EFCC00A".getBytes();
	
	private boolean hasFirstKey;
	private boolean hasSecondKey;
	
    public ShutdownBehavior(GreenRuntime runtime, long keyFieldId) {
    	this.keyFieldId = keyFieldId;
		this.channel = runtime.newCommandChannel().newHTTPResponseService(1000,100);

	}
	    	
	@Override
	public boolean acceptShutdown() {
		return hasFirstKey & hasSecondKey;
	}
	
	@Override
	public boolean restRequest(HTTPRequestReader request) {

		if (request.structured().isEqual(keyFieldId, PASS1)) {
			if (channel.publishHTTPResponse(request, 200)) {
				channel.requestShutdown();
				hasFirstKey = true;
				System.out.println("first key");
				return true;
			}
		} else if (hasFirstKey && request.structured().isEqual(keyFieldId, PASS2)) {
			if (channel.publishHTTPResponse(request, 200)) {
				hasSecondKey = true;
				System.out.println("second key");
				return true;
			}
		} else {
			return channel.publishHTTPResponse(request, 404);
		}
		return false;
	}
	
}
