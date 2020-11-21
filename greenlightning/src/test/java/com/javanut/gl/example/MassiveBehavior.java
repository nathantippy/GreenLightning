package com.javanut.gl.example;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenCommandChannel;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubListener;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.TimeListener;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.ChannelWriter;
import com.javanut.pronghorn.pipe.Writable;

public class MassiveBehavior implements GreenApp {

	public static void main(String[] args) {
		GreenRuntime.run(new MassiveBehavior());
	}
	
	@Override
	public void declareConfiguration(GreenFramework builder) {
		builder.setTimerPulseRate(500);//1);//TimeTrigger.OnTheSecond);
		builder.enableTelemetry();

	}

	@Override
	public void declareBehavior(GreenRuntime runtime) {
		
		//runtime.addTimePulseListener(new stopperBehavior(runtime));
		
		int i = 7;
		while (--i>=0) {
			final GreenCommandChannel cmd = runtime.newCommandChannel();
			final PubSubService pubSubServce = cmd.newPubSubService();
			
			final String topic = "topic"+i;
			final int value = i;
			
			final Writable writable = new Writable() {

				@Override
				public void write(ChannelWriter writer) {
					writer.writePackedInt(value);
				}
				
			};
			
			TimeListener pubs = new TimeListener() {

				@Override
				public void timeEvent(long time, int iteration) {
					if (!pubSubServce.publishTopic(topic, writable)) {
						System.out.println("overloaded can not publish "+value);
					}
				}
				
			};
			runtime.addTimePulseListener(pubs);
			
			PubSubListener subs = new PubSubListener() {
				
				public boolean message(CharSequence topic, ChannelReader payload) {
					
					
					return true;
				}
			};
	
			runtime
			 .addPubSubListener(subs)
			 .addSubscription(topic);
			 
			
		}
	
	
	}

}
