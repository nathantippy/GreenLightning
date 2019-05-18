package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.HTTPResponseListener;
import com.javanut.gl.api.HTTPResponseReader;
import com.javanut.gl.api.Payloadable;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class HTTPResponse implements HTTPResponseListener {

	
	
	private AppendableProxy console;

	public HTTPResponse(Appendable console) {
		this.console = Appendables.wrap(console);
	}
	
	@Override
	public boolean responseHTTP(HTTPResponseReader reader) {
		
		Appendables.appendValue(console, " status:",  reader.statusCode(),"\n");
		
		console.append("   type:").append(reader.contentType().toString()).append("\n");
	
		
		Payloadable payload = new Payloadable() {
			@Override
			public void read(ChannelReader reader) {
				if (reader.available()<1) {
					//error
					return;
				}
				int age = reader.structured().readInt(Fields.AGE);
				String name = reader.structured().readText(Fields.NAME);
				
				Appendables.appendValue(console.append(name).append(" "),age).append("\n");
				
			}
		};
		boolean hadAbody = reader.openPayloadData(payload );

		
		return true;
	}

}
