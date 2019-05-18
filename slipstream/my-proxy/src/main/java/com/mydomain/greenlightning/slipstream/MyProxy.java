package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.api.ClientHostPortInstance;
import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPClientConfig;
import com.javanut.pronghorn.network.HTTPServerConfig;
import com.javanut.pronghorn.stage.scheduling.GraphManager;

public class MyProxy implements GreenApp {

    private ClientHostPortInstance session;
	private String HANDOFF_TOPIC = "handoff";
	private final boolean useTLS;
	private final int port;
	
	public MyProxy(boolean useTLS, int port) {
		this.useTLS = useTLS;
		this.port = port;
	}
	
	
	@Override
    public void declareConfiguration(GreenFramework builder) {
   	
    	HTTPServerConfig server = builder.useHTTP1xServer(port)
    	.setConcurrentChannelsPerDecryptUnit(4)
    	.setConcurrentChannelsPerEncryptUnit(4)
    	       .setHost("127.0.0.1");
    	
    	if (!useTLS) {
    		server.useInsecureServer();
    	}
    	
    	
    	builder.defineRoute().path("/proxy").routeId(Struct.PROXY_ROUTE);
    	builder.defineRoute().path("/service").routeId(Struct.SERIVCE_ROUTE);
        
    	HTTPClientConfig client = 
    			useTLS 
    			? builder.useNetClient() 
    			: builder.useInsecureNetClient();
    	    	    	
    	session = client.newHTTPSession("127.0.0.1", port).finish();
    	
    	GraphManager.showThreadIdOnTelemetry = true;
    	GraphManager.showScheduledRateOnTelemetry = true;
    	
      //  builder.enableTelemetry();
         
    }


    @Override
    public void declareBehavior(GreenRuntime runtime) {
 
    	runtime.addRestListener(new ExternalRequest(runtime, session, "/service", HANDOFF_TOPIC))
    	                               .includeRoutesByAssoc(Struct.PROXY_ROUTE);

    	runtime.addPubSubListener(new ExternalResponse(runtime))
    	                               .acceptHostResponses(session)
    	                               .addSubscription(HANDOFF_TOPIC);
    	
    	runtime.addRestListener(new InternalService(runtime))
    	 							   .includeRoutesByAssoc(Struct.SERIVCE_ROUTE);
    	    	
    	
    }
  
}
