package com.javanut.oe.greenlightning.api.behaviors;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.PubSubMethodListener;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.WaitFor;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.Writable;

public class IngressBehavior implements PubSubMethodListener {


	private PubSubFixedTopicService cmd;

	public IngressBehavior(GreenRuntime runtime, String publishTopic) {
		cmd = runtime.newCommandChannel().newPubSubService(publishTopic);

	}

	public boolean receiveMqttMessage(CharSequence topic, ChannelReader payload) {
		// this received when mosquitto_pub is invoked - see MQTTClient
		System.out.print("\ningress body: ");

		// Read the message payload and output it to System.out
		payload.readUTFOfLength(payload.available(), System.out);
		System.out.println();

		// Create the on-demand mqtt payload writer
		Writable mqttPayload = writer -> writer.writeUTF("\nsecond step test message");

		// On the 'localtest' topic publish the mqtt payload
		cmd.publishTopic(mqttPayload, WaitFor.None);

		// We consumed the message
		return true;
	}
}
