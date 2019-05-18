package com.javanut.gl.api;

public interface ExtractedJSONFieldsForClient extends ExtractedJSONFields<ExtractedJSONFieldsForClient> {

	public ClientHostPortInstance finish();
	public ExtractedJSONFieldsForClient setTimeoutNS(long timeoutNS);
	
}
