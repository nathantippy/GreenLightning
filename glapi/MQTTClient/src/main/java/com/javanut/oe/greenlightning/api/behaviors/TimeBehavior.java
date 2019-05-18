package com.javanut.oe.greenlightning.api.behaviors;

import java.util.Date;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.TimeListener;
import com.javanut.gl.api.WaitFor;
import com.javanut.gl.api.Writable;

public class TimeBehavior implements TimeListener {
	private int droppedCount = 0;
    private final PubSubFixedTopicService cmdChnl;

	private long total = 0;

	public TimeBehavior(GreenRuntime runtime, String publishTopic) {
		cmdChnl = runtime.newCommandChannel().newPubSubService(publishTopic);

	}

	@Override
	public void timeEvent(long time, int iteration) {
		int i = 1;//iterations
		while (--i>=0) {
			Date d = new Date(System.currentTimeMillis());
			
			// On the timer event create a payload with a string encoded timestamp
			Writable writable = writer -> writer.writeUTF8Text("'MQTT egress body " + d + "'");
					
			// Send out the payload with thre MQTT topic "topic/egress"
			boolean ok = cmdChnl.publishTopic(writable, WaitFor.None);
			if (ok) {
				System.err.println("sent "+d+" total "+(++total));
			}
			else {
				droppedCount++;
				System.err.println("The system is backed up, dropped "+droppedCount);
			}
		}
	}
}
