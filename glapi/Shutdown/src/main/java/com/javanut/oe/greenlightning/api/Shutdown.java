package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.network.HTTPServerConfig;
import com.javanut.pronghorn.network.NetGraphBuilder;


public class Shutdown implements GreenApp
{	
	private final String host;
	private final int port;
	private long keyFieldId;
	
	public Shutdown(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public Shutdown() {
		this.host = null;
		this.port = 8443;
	}
	
    @Override    
    public void declareConfiguration(GreenFramework c) {
    	
    	HTTPServerConfig conf = c.useHTTP1xServer(port)
    			.setHost(NetGraphBuilder.bindHost(host))
    			.setDecryptionUnitsPerTrack(4)
    			.setDefaultPath("");
    	    	
    	int aRouteId = c.defineRoute().path("/shutdown?key=${key}").routeId();
    	
    	keyFieldId = c.lookupFieldByName(aRouteId, "key");
    }
  
    @Override
    public void declareBehavior(final GreenRuntime runtime) {
    	runtime.registerListener(new ShutdownBehavior(runtime, keyFieldId)).includeAllRoutes();	
    }          
          
}
