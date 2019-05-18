package com.javanut.gl.impl;

import com.javanut.gl.api.HTTPRequestReader;
import com.javanut.gl.api.RestMethodListener;
import com.javanut.pronghorn.network.ServerCoordinator;

/**
 * Functional interface for a handler of REST service events.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface RestListenerBase extends RestMethodListenerBase {

	public static final int END_OF_RESPONSE = ServerCoordinator.END_RESPONSE_MASK;
	public static final int CLOSE_CONNECTION = ServerCoordinator.CLOSE_CONNECTION_MASK;
		

    boolean restRequest(HTTPRequestReader request);
    
    
}
