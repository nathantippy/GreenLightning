package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.json.encode.JSONRenderer;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.StructuredReader;
import com.javanut.pronghorn.util.AppendableProxy;

public class RestBehaviorJsonResponce implements RestListener {

	private static final JSONRenderer<RestBehaviorJsonResponce> jsonRenderer = new JSONRenderer<RestBehaviorJsonResponce>()
			  .startObject()	  
			  .string("name", (o,t)->t.append(o.name))
			  .bool("isLegal", o->o.isLegal)
			  .endObject();

	private StringBuilder name = new StringBuilder();
	private boolean isLegal = false;
	
	private HTTPResponseService responseService;
	  
	public RestBehaviorJsonResponce(GreenRuntime runtime, AppendableProxy console) {
		responseService = runtime.newCommandChannel().newHTTPResponseService(4, 1<<10);
		
	}

	@Override
	public boolean restRequest(HTTPRequestReader request) {
		StructuredReader record = request.structured();
		
		int age = record.readInt(Field.PERSON_AGE);
		isLegal = age>=21;
		
		name.setLength(0);
		record.readText(Field.PERSON_NAME, name);
				
		return responseService.publishHTTPResponse(request, 200, 
				 HTTPContentTypeDefaults.JSON, w-> {
					 jsonRenderer.render(w, this);
				 });
	}

}
