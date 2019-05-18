package com.javanut.gl.api;

import com.javanut.pronghorn.network.http.CompositeRoute;

public interface RouteDefinition {

	ExtractedJSONFieldsForRoute parseJSON();
			
	CompositeRoute path(CharSequence path);
	
}
