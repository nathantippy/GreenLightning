package com.javanut.gl.example;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;

public class AllRoutesExample2 implements GreenApp {

	public static void main(String[] args) {
		GreenRuntime.run(new AllRoutesExample2(),args);
	}

	@Override
	public void declareConfiguration(GreenFramework builder) {
		builder.useHTTP1xServer(8082);
		int a = builder.defineRoute().path("/routeOne").routeId();
		int b = builder.defineRoute().path("/second?a=#{value}").routeId();
		int c = builder.defineRoute().path("/woot").routeId();
		
		System.err.println(a+"  "+b+"  "+c);

	}

	@Override
	public void declareBehavior(GreenRuntime runtime) {
		
		final GreenCommandChannel cmd = runtime.newCommandChannel();	
		final HTTPResponseService responseService = cmd.newHTTPResponseService();
		
		RestListener listener = new RestListener() {
			@Override
			public boolean restRequest(HTTPRequestReader request) {	
				
				//TODO: this route is the wrong value
				int id = request.getRouteId();
				System.out.println(id);
			//	request.getText("value".getBytes(), System.out);
				
				return responseService.publishHTTPResponse(request, 200);				
			}			
		};
		runtime.addRestListener(listener).includeAllRoutes();
	}
}
