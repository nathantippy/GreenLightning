package com.javanut.gl.api;

import com.javanut.gl.impl.http.server.HTTPPayloadReader;
import com.javanut.gl.impl.stage.HeaderTypeCapture;
import com.javanut.pronghorn.network.ServerCoordinator;
import com.javanut.pronghorn.network.config.HTTPContentType;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.network.config.HTTPHeaderDefaults;
import com.javanut.pronghorn.network.config.HTTPSpecification;
import com.javanut.pronghorn.network.schema.NetResponseSchema;
import com.javanut.pronghorn.pipe.Pipe;

public class HTTPResponseReader extends HTTPPayloadReader<NetResponseSchema> {

	private short status;
	private int flags;
	private long connectionId;
	private int clientSessionId;
	private HeaderTypeCapture htc;

	public HTTPResponseReader(Pipe<NetResponseSchema> pipe, HTTPSpecification<?,?,?,?> httpSpec) {
		super(pipe);
		this.httpSpec = httpSpec;
		
		if (null==htc) {
			this.htc  = new HeaderTypeCapture(httpSpec);
		}
	}

	public void setStatusCode(short statusId) { //TODO: hide these so maker does not see them.
		this.status = statusId;
	}
	
	/**
    * statusCode Status code of the response. -1 indicates
    *                   the network connection was lost.
    *   @return status code                
    */                   
	public short statusCode() {
		return this.status;
	}

	/**
	 *
	 * @return  HTTPContentTypeDefaults.UNKNOWN or htc.type
	 */
	public HTTPContentType contentType() {
					
	   	 if (structured().identityVisit(HTTPHeaderDefaults.CONTENT_TYPE, htc)) {
			 return htc.type();
		 } else {
			 return HTTPContentTypeDefaults.UNKNOWN;
		 }
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	public final boolean isBeginningOfResponse() {
		return 0 != (this.flags&ServerCoordinator.BEGIN_RESPONSE_MASK);
	}
	
	public final boolean isEndOfResponse() {
		return 0 != (this.flags&ServerCoordinator.END_RESPONSE_MASK);
	}
	
	public final boolean isConnectionClosed() {
		return 0 != (this.flags&ServerCoordinator.CLOSE_CONNECTION_MASK);
	}

	/**
	 *
	 * @param ccId1 long arg to be used as connection id
	 */
	public void setConnectionId(long ccId1) {
		connectionId = ccId1;
	}

	/**
	 *
	 * @return connectionId
	 */
	public long connectionId() {
		return connectionId;
	}

	/**
	 * 
	 * @param sessionId client session id which will not change at runtime
	 */
	public void setClientSessionId(int sessionId) {
		clientSessionId = sessionId;
	}
	
	/**
	 * 
	 * @return client session id for this call.
	 */
	public int sessionId() {
		return clientSessionId;
	}


	
	
}