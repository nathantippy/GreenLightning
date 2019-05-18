package com.javanut.gl.impl.http.server;

import com.javanut.gl.api.HTTPResponseReader;
import com.javanut.pronghorn.network.config.HTTPContentType;


/**
 * Functional interface for HTTP responses returned from outgoing
 * HTTP requests.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface HTTPResponseListenerBase {
    /**
     * Invoked when an HTTP response is received by this listener.
     * 
     */
	boolean responseHTTP(HTTPResponseReader reader);

}
