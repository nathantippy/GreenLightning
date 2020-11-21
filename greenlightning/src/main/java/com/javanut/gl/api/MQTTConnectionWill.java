package com.javanut.gl.api;

import com.javanut.pronghorn.pipe.Writable;

public class MQTTConnectionWill {
	public boolean latWillRetain = false;
	public MQTTQoS lastWillQoS = MQTTQoS.atMostOnce;
	public CharSequence lastWillTopic = null;
	public Writable lastWillPayload = null;
	public CharSequence connectFeedbackTopic = null;
}
