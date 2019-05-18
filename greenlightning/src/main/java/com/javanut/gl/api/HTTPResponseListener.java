package com.javanut.gl.api;

import com.javanut.gl.impl.http.server.HTTPResponseListenerBase;

/**
 * Functional interface for HTTP responses returned from outgoing
 * HTTP requests.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface HTTPResponseListener extends Behavior, HTTPResponseListenerBase {

	

}
