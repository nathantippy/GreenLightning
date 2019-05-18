package com.javanut.gl.api;

public interface RouteFilter<T extends RouteFilter<T>> {
	
	T includeRoutesByAssoc(Object ... assocRouteObjects);
	
	T includeRoutes(int ... routeIds);
	
	T excludeRoutes(int ... routeIds);

	T includeAllRoutes();
	
}
