package com.javanut.oe.greenlightning.api.behaviors;

import com.javanut.gl.api.PubSubMethodListener;
import com.javanut.pronghorn.pipe.ChannelReader;

public class EgressBehavior implements PubSubMethodListener {

	public boolean receiveTestTopic(CharSequence topic, ChannelReader payload) {
		// topic is the MQTT topic
		// payload is the MQTT payload
		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.println("got topic "+topic+" payload "+payload.readUTF()+"\n");

		return true;
	}
}
