package com.javanut.gl.impl.http.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.HeaderReader;
import com.javanut.gl.impl.PayloadReader;
import com.javanut.pronghorn.network.config.HTTPContentType;
import com.javanut.pronghorn.network.config.HTTPHeader;
import com.javanut.pronghorn.network.config.HTTPRevision;
import com.javanut.pronghorn.network.config.HTTPSpecification;
import com.javanut.pronghorn.network.config.HTTPVerb;
import com.javanut.pronghorn.pipe.MessageSchema;
import com.javanut.pronghorn.pipe.Pipe;

public class HTTPPayloadReader<S extends MessageSchema<S>> extends PayloadReader<S> implements HeaderReader {


	protected HTTPSpecification<
			? extends Enum<? extends HTTPContentType>,
			? extends Enum<? extends HTTPRevision>,
			? extends Enum<? extends HTTPVerb>,
			? extends Enum<? extends HTTPHeader>> httpSpec;

	
	private static final Logger logger = LoggerFactory.getLogger(HTTPPayloadReader.class);
	

	public HTTPPayloadReader(Pipe<S> pipe) {
		super(pipe);
	}


	public HTTPSpecification<
			? extends Enum<? extends HTTPContentType>,
			? extends Enum<? extends HTTPRevision>,
			? extends Enum<? extends HTTPVerb>,
			? extends Enum<? extends HTTPHeader>> getSpec() {
		return this.httpSpec;
	}


}
