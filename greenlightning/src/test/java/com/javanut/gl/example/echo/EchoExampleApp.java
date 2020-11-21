package com.javanut.gl.example.echo;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.pronghorn.network.config.HTTPHeaderDefaults;
import com.javanut.pronghorn.network.http.HeaderWritable;
import com.javanut.pronghorn.network.http.HeaderWriter;
import com.javanut.pronghorn.pipe.ChannelWriter;
import com.javanut.pronghorn.pipe.Writable;

public class EchoExampleApp implements GreenApp {

	private final Appendable target;
	public EchoExampleApp(Appendable target) {
		this.target = target;
	}
	
	@Override
	public void declareConfiguration(GreenFramework builder) {
		builder.useHTTP1xServer(6084)
	       .useInsecureServer()
	       .echoHeaders(128, HTTPHeaderDefaults.DNT, HTTPHeaderDefaults.STRICT_TRANSPORT_SECURITY)
	       .setDecryptionUnitsPerTrack(4)
	       .setEncryptionUnitsPerTrack(4)
	       .setHost("127.0.0.1");
		
		builder.defineRoute()
	       .path("/test")
		   .routeId();
		
		builder.enableTelemetry();
	}	

	@Override
	public void declareBehavior(GreenRuntime runtime) {
		HTTPResponseService resp = runtime.newCommandChannel().newHTTPResponseService();		
		runtime.addRestListener("restListener",(r)->{	
			
			HeaderWritable headers = new HeaderWritable() {
				@Override
				public void write(HeaderWriter writer) {
					
					writer.write(HTTPHeaderDefaults.DNT, "true");
					writer.write(HTTPHeaderDefaults.STRICT_TRANSPORT_SECURITY, "hello");
					
				}
			};
			Writable writable = new Writable() {
				@Override
				public void write(ChannelWriter writer) {
					//no response
				}
			};
			return resp.publishHTTPResponse(r, headers, null, writable);
			
		}).includeAllRoutes();
		
	}

}
