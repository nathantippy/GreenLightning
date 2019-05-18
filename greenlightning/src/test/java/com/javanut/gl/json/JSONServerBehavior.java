package com.javanut.gl.json;

import static org.junit.Assert.assertEquals;

import com.javanut.gl.api.Builder;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.HTTPResponseService;
import com.javanut.gl.api.RestListener;
import com.javanut.gl.api.Writable;
import com.javanut.gl.json.JSONRequest.Fields;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.pipe.ChannelWriter;

public class JSONServerBehavior implements RestListener {

    private final JSONResponse response = new JSONResponse();
    private final JSONRequest jsonRequest = new JSONRequest();
    private final long flagsFieldId;
	private final HTTPResponseService channel;
    
    static int defineRoute(Builder builder) {
        return builder.defineRoute().parseJSON()
	        		.stringField("ID1", Fields.ID1)
	        		.stringField("ID2", Fields.ID2)
	        		.stringField("TimeStamp", Fields.TimeStamp)
	        		.integerField("Value", Fields.Value)
        		.path("/test/path")
        		.path("/test/path?flag=#{flag}")
                .defaultInteger("flag", -6)
                .routeId();
    }

    JSONServerBehavior(GreenRuntime runtime, long flagsFieldId) {
    	this.flagsFieldId = flagsFieldId;
        this.channel = runtime.newCommandChannel().newHTTPResponseService();
      
    }

    @Override
    public boolean restRequest(HTTPRequestReader request) {
        int f = request.structured().readInt(flagsFieldId);

        request.openPayloadData(reader -> {
            jsonRequest.reset();
            jsonRequest.readFromJSON(reader);
        });

        System.out.println("Server: " + f + " " + jsonRequest);

        if (f == 42) assertEquals(42, jsonRequest.getValue());
        if (f == -6) assertEquals(43, jsonRequest.getValue());

        channel.publishHTTPResponse(
                request.getConnectionId(), request.getSequenceCode(),
                200, false, HTTPContentTypeDefaults.JSON,
                new Writable() {
                    @Override
                    public void write(ChannelWriter writer) {
                    	
                        //System.err.println("pre "+writer.length());
                        
                    	response.writeToJSON(writer);
                        
                    	//System.err.println("post "+writer.length());
                    	
                        
                    }
                });
        
        return true;
    }
}
