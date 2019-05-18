package com.javanut.gl.api;

public interface BridgeConfig<T,S> {

	T transmissionConfigurator(long id);
	S subscriptionConfigurator(long id);

}
