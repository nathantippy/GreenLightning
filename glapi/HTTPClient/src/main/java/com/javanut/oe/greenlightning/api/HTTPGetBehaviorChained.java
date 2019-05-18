package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.ClientHostPortInstance;
import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestService;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.StartupListener;

public class HTTPGetBehaviorChained implements StartupListener {
	
	private HTTPRequestService cmd;

    private ClientHostPortInstance session;

	private PubSubService x;
	
	public HTTPGetBehaviorChained(GreenRuntime runtime, ClientHostPortInstance session) {
		GreenCommandChannel newCommandChannel = runtime.newCommandChannel();
		this.cmd = newCommandChannel.newHTTPClientService();
		this.x = newCommandChannel.newPubSubService();
		
		this.session = session;
	}

	@Override
	public void startup() {
		
		cmd.httpGet(session, "/testPageB");
		
	}

}
