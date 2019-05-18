package com.javanut.gl.api;

import com.javanut.gl.impl.http.server.HTTPPayloadReader;
import com.javanut.pronghorn.network.config.HTTPVerbDefaults;
import com.javanut.pronghorn.network.http.FieldExtractionDefinitions;
import com.javanut.pronghorn.network.http.HTTPRouterStageConfig;
import com.javanut.pronghorn.network.schema.HTTPRequestSchema;
import com.javanut.pronghorn.pipe.ChannelWriter;
import com.javanut.pronghorn.pipe.Pipe;

public class HTTPRequestReader extends HTTPPayloadReader<HTTPRequestSchema> implements HTTPFieldReader<HTTPRequestSchema> {

	private long connectionId;
	private long sequenceCode;
	private int revisionId;
	private int routeId;
	private Object routeAssoc;

	private HTTPVerbDefaults verb;
	private final boolean hasNoRoutes;

	private HTTPRouterStageConfig<?, ?, ?, ?> http1xRouterStageConfig;

	public HTTPRequestReader(Pipe<HTTPRequestSchema> pipe, boolean hasNoRoutes,
			                 HTTPRouterStageConfig<?, ?, ?, ?> http1xRouterStageConfig) {
		super(pipe);
		this.hasNoRoutes = hasNoRoutes;
		this.httpSpec = http1xRouterStageConfig.httpSpec;
		this.http1xRouterStageConfig = http1xRouterStageConfig;
	}
	
	public void setVerb(HTTPVerbDefaults verb) {
		this.verb = verb;
	}

	public HTTPVerbDefaults getVerb() {
		return this.verb;
	}
	

	public boolean isVerbGet() {
		return HTTPVerbDefaults.GET == verb;
	}
	
	public boolean isVerbConnect() {
		return HTTPVerbDefaults.CONNECT == verb;
	}
	
	public boolean isVerbDelete() {
		return HTTPVerbDefaults.DELETE == verb;
	}

	public boolean isVerbHead() {
		return HTTPVerbDefaults.HEAD == verb;
	}

	public boolean isVerbOptions() {
		return HTTPVerbDefaults.OPTIONS == verb;
	}

	public boolean isVerbPatch() {
		return HTTPVerbDefaults.PATCH == verb;
	}

	public boolean isVerbPost() {
		return HTTPVerbDefaults.POST == verb;
	}
	
	public boolean isVerbPut() {
		return HTTPVerbDefaults.PUT == verb;
	}
	
	public boolean isVerbTrace() {
		return HTTPVerbDefaults.TRACE == verb;
	}

	public void setRouteId(int routeId, Object routeAssoc) {
		this.routeId = routeId;
		this.routeAssoc = routeAssoc;
	}

	public int getRouteId() {
		return routeId;
	}
	
	public void setConnectionId(long connectionId, long sequenceCode) {
		this.connectionId = connectionId;
		this.sequenceCode = sequenceCode;
	}
	
	public void handoff(ChannelWriter writer) {
		writer.writePackedLong(connectionId);
		writer.writePackedLong(sequenceCode);
	}
	
	public long getConnectionId() {
		return connectionId;
	}
	
	public long getSequenceCode() {
		return sequenceCode;
	}


	public void setRevisionId(int value) {
		revisionId = value;
	}
	
	public int getRevisionId() {
		return revisionId;
	}

	
	/**
	 * Only call this method when NO routes have been defined.
	 * @param <A> target appendable type
	 * @param appendable
	 * 
	 */
	public <A extends Appendable> A getRoutePath(A appendable) {
    	assert(isStructured());
    	assert(getStructType(this) == http1xRouterStageConfig.UNMAPPED_STRUCT);
    	
		if (hasNoRoutes) {		
			return structured().readText(http1xRouterStageConfig.unmappedPathField,appendable);		
		} else {
			throw new UnsupportedOperationException("this method can only be used when no routes have been defined.");
		}
	}

	public Object getRouteAssoc() {
		return routeAssoc;
	}
	




}
