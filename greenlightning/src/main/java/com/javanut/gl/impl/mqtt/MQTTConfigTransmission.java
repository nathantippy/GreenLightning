package com.javanut.gl.impl.mqtt;

import com.javanut.gl.api.MQTTQoS;

public interface MQTTConfigTransmission {

	public MQTTConfigTransmission setQoS(MQTTQoS qos);
	public MQTTConfigTransmission setRetain(boolean retain);
}
