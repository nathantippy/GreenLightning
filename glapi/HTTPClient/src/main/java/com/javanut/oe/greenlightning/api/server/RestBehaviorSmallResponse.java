package com.javanut.oe.greenlightning.api.server;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.json.encode.JSONRenderer;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.util.AppendableProxy;

public class RestBehaviorSmallResponse implements RestListener {

	private final HTTPResponseService cmd;
	private final AppendableProxy console;
	
    public static final JSONRenderer<Object> renderer = new JSONRenderer<Object>()
            .startObject()
            .integer("age", (x) -> 123)
            .string("name", (x,w) -> {w.append("bob");})
            .endObject();
    
	public RestBehaviorSmallResponse(GreenRuntime runtime, AppendableProxy console) {	
		this.cmd = runtime.newCommandChannel().newHTTPResponseService(4,400);
		this.console = console;
		
	}
	
	@Override
	public boolean restRequest(HTTPRequestReader request) {
		
		if (request.isVerbPost()) {
			request.openPayloadData((reader)->{
				
				console.append("POST: ");
				reader.readUTFOfLength(reader.available(),console);
								
			});
		}

		//if this can not be published then we will get the request again later to be reattempted.
		return cmd.publishHTTPResponse(request, 200, 
								false,
				                HTTPContentTypeDefaults.TXT,
				                (writer)-> {
				                	renderer.render(writer, this);
				                });

	}

}
