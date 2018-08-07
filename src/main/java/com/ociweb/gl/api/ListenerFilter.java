package com.ociweb.gl.api;

import com.ociweb.gl.impl.stage.CallableMethod;
import com.ociweb.gl.impl.stage.CallableRestRequestReader;
import com.ociweb.gl.impl.stage.CallableStaticMethod;
import com.ociweb.gl.impl.stage.CallableStaticRestRequestReader;

public interface ListenerFilter extends RouteFilter<ListenerFilter> {


	<T extends Behavior> ListenerFilter addSubscription(CharSequence topic, CallableStaticMethod<T> method);
	
	ListenerFilter addSubscription(CharSequence topic, CallableMethod method);
		
	ListenerFilter isolate();
	
	<T extends Behavior> ListenerFilter includeRoute(int routeId, CallableStaticRestRequestReader<T> callable);
		
	ListenerFilter includeRoute(int routeId, CallableRestRequestReader callable);
	
	public ListenerFilter includeRoutesByAssoc(Object ... assocRouteObjects);
	public ListenerFilter includeRoutesByAssoc(Object assocRouteObject, final CallableRestRequestReader callable);
	public <T extends Behavior> ListenerFilter includeRoutesByAssoc(Object assocRouteObject, final CallableStaticRestRequestReader<T> callable);
	
	/**
	 * Add subscription to this topic to this listener at startup.
	 * @param topic
	 */
	ListenerFilter addSubscription(CharSequence topic); 
	
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter includeStateChangeTo(E ... states); 

	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter excludeStateChangeTo(E ... states); 

	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter includeStateChangeFrom(E ... states); 
	
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter includeStateChangeToAndFrom(E ... states); 
		
	/**
	 * For StateChangeListener reduce notifications.
	 * @param states
	 */
	<E extends Enum<E>> ListenerFilter excludeStateChangeFrom(E ... states);

	@Deprecated
	int getId();//TODO: remove?  not sure this should be used...	
	
	<E extends Enum<E>> ListenerFilter acceptHostResponses(ClientHostPortInstance ... httpSessions);

	ListenerFilter SLALatencyNS(long latency);


	
}
