package com.ociweb.gl.api;

public class HTTPSession extends ClientHostPortInstance{
	
	public HTTPSession(String host, int port, int sessionId) {
		super(host,port,sessionId);
	}
	
	public HTTPSession(String host, int port) {
		super(host,port);
	}
}
