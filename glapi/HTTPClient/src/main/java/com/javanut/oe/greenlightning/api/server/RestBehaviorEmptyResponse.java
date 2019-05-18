package com.javanut.oe.greenlightning.api.server;

import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.pronghorn.network.config.HTTPHeaderDefaults;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class RestBehaviorEmptyResponse implements RestListener {

	private final int cookieHeader = HTTPHeaderDefaults.COOKIE.ordinal();
	private final HTTPResponseService cmd;
	private final AppendableProxy console;
	private final long nameFieldId;
	
	
	public RestBehaviorEmptyResponse(GreenRuntime runtime, long nameFieldId, AppendableProxy console) {
		this.nameFieldId = nameFieldId;		
		this.cmd = runtime.newCommandChannel().newHTTPResponseService(4,400);
		this.console = console;
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
	    int argInt = request.structured().readInt(nameFieldId);
	    Appendables.appendValue(console, "Arg Int: ", argInt, "\n");
	    		
	    
	    request.structured().identityVisit(HTTPHeaderDefaults.COOKIE, (id,reader,field)-> {
			
			console.append("COOKIE: ");
			reader.readUTF(console).append('\n');
					
		});
		
		if (request.isVerbPost()) {
			request.openPayloadData((reader)->{
				
				console.append("POST: ");
				reader.readUTFOfLength(reader.available(), console);
				console.append('\n');
									
			});
		}
		
		//no body just a 200 ok response.
		return cmd.publishHTTPResponse(request, 200);

	}

}
