package com.javanut.gl.impl;

import com.javanut.gl.api.BridgeConfig;
import com.javanut.gl.api.MsgRuntime;
import com.javanut.gl.impl.stage.EgressConverter;
import com.javanut.gl.impl.stage.IngressConverter;

public abstract class BridgeConfigImpl<T,S> implements BridgeConfig<T,S> {

	public abstract long addSubscription(CharSequence internalTopic, CharSequence externalTopic);
	public abstract long addSubscription(CharSequence internalTopic, CharSequence externalTopic, IngressConverter converter);
	
	public abstract long addTransmission(CharSequence internalTopic, CharSequence externalTopic);
	public abstract long addTransmission(CharSequence internalTopic, CharSequence externalTopic, EgressConverter converter);
	
	public long addSubscription(CharSequence topic) {
		return addSubscription(topic,topic);
	}

	public long addTransmission(CharSequence topic) {
		return addTransmission(topic, topic);
	}

	public abstract void finalizeDeclareConnections(MsgRuntime<?,?,?> msgRuntime);
	
}
