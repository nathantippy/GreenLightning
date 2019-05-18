package com.javanut.gl.api;

import com.javanut.gl.impl.BuilderImpl;
import com.javanut.pronghorn.network.schema.HTTPRequestSchema;
import com.javanut.pronghorn.pipe.Pipe;

public class StageRouteFilter implements RouteFilter {

	private final Pipe<HTTPRequestSchema> input;
    private final int parallelIndex;
    private final BuilderImpl builder;
    
	public StageRouteFilter(Pipe<HTTPRequestSchema> input, BuilderImpl builder, int parallelIndex) {
		this.input = input;
		this.builder = builder;
		this.parallelIndex = parallelIndex;
	}
		
	@Override
	public RouteFilter includeRoutes(int... routeIds) {
		
		builder.appendPipeMappingIncludingGroupIds(input, parallelIndex, routeIds);
	    
		return this;
	}
	
	@Override
	public RouteFilter includeRoutesByAssoc(Object ... assocRouteObjects) {
		
		int r = assocRouteObjects.length;
		int[] routeIds = new int[r];
		while (--r >= 0) {
			routeIds[r] = builder.routerConfig().lookupRouteIdByIdentity(assocRouteObjects[r]);
		}		
		
		builder.appendPipeMappingIncludingGroupIds(input, parallelIndex, routeIds);
	    
		return this;
	}
	

	@Override
	public RouteFilter excludeRoutes(int... routeIds) {
		
		builder.appendPipeMappingExcludingGroupIds(input, parallelIndex, routeIds);

		return this;
	}


	@Override
	public RouteFilter includeAllRoutes() {
		
		if (0==builder.routerConfig().totalRoutesCount()) {
			builder.defineRoute().path("${path}").routeId();
		}		
		int[] routes = new int[builder.routerConfig().totalRoutesCount()];
		int i = routes.length;
		while (--i>=0) {
			routes[i]=i;
		}
		includeRoutes(routes);
		
		return this;
	}

}
