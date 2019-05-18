package com.javanut.gl.impl;

import com.javanut.gl.api.ExtractedJSONFieldsForRoute;
import com.javanut.gl.api.RouteDefinition;
import com.javanut.pronghorn.network.config.HTTPHeader;
import com.javanut.pronghorn.network.http.CompositeRoute;
import com.javanut.pronghorn.network.http.HTTPRouterStageConfig;

final class RouteDefinitionImpl implements RouteDefinition {

	private final HTTPRouterStageConfig<?,?,?,?> config;
	private final HTTPHeader[] headers;
	private CompositeRoute route = null;

	
	RouteDefinitionImpl(HTTPRouterStageConfig<?,?,?,?> config, HTTPHeader[] headers) {
		this.config = config;
		this.headers = headers;
	}

	@Override
	public CompositeRoute path(CharSequence path) {
		return (null==route) ? this.config.registerCompositeRoute(headers).path(path) :  route;
	}

	@Override
	public ExtractedJSONFieldsForRoute parseJSON() {				
		return new JSONRouteDefImpl();		
	}	
	
	class JSONRouteDefImpl extends JSONExtractorDefImpl<JSONRouteDefImpl> implements ExtractedJSONFieldsForRoute {
			@Override
			public CompositeRoute path(CharSequence path) {			
				return route = RouteDefinitionImpl.this.config.registerCompositeRoute(ex.finish(), headers).path(path);
			}			
	}
	
	
}