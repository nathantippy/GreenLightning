package com.javanut.gl.network;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.Writable;
import com.javanut.pronghorn.network.config.HTTPHeaderDefaults;
import com.javanut.pronghorn.network.http.HeaderWritable;

public class OpenCloseTestServer implements GreenApp {

	private final Appendable target;
	private final int port;
	private final boolean telemetry;
	private int neverCloseRoute;
	private int alwaysCloseRoute;
	
	public OpenCloseTestServer(int port, boolean telemetry, Appendable target) {
		this.target = target;
		this.port = port;
		this.telemetry = telemetry;
	}

	@Override
	public void declareConfiguration(GreenFramework builder) {
		
		if (telemetry) {
			builder.enableTelemetry(8076);
		}
		
		builder.useHTTP1xServer(port)
			   .setHost("127.0.0.1")
			   .setMaxConnectionBits(3)//only 8 connections
			   .setDecryptionUnitsPerTrack(6)
			   .setEncryptionUnitsPerTrack(6)
		       .useInsecureServer();
		
		neverCloseRoute = builder.defineRoute().path("neverclose").routeId();
		alwaysCloseRoute = builder.defineRoute().path("alwaysclose").routeId();
				       
	}

	@Override
	public void declareBehavior(GreenRuntime runtime) {

		HTTPResponseService respClose = runtime.newCommandChannel().newHTTPResponseService();

		runtime.addRestListener((r) -> {
			
			HeaderWritable headers = (w)->{
				w.write(HTTPHeaderDefaults.CONNECTION, "close");
			};
			
			return respClose.publishHTTPResponse(r, headers, null, Writable.NO_OP);

		}).includeRoutes(alwaysCloseRoute);

		HTTPResponseService respOpen = runtime.newCommandChannel().newHTTPResponseService();

		runtime.addRestListener((r) -> {
			respOpen.publishHTTPResponse(r, 200);
			return true;
		}).includeRoutes(neverCloseRoute);
	}

}
