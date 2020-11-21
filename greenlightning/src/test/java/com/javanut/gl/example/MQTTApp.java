package com.javanut.gl.example;

import java.util.Date;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.MQTTBridge;
import com.javanut.gl.api.MQTTQoS;
import com.javanut.gl.api.MsgCommandChannel;
import com.javanut.gl.api.PubSubListener;
import com.javanut.gl.api.PubSubService;
import com.javanut.gl.api.TimeListener;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.ChannelWriter;
import com.javanut.pronghorn.pipe.Writable;

public class MQTTApp implements GreenApp {

	private MQTTBridge mqttConfig;
	
	//monitor    mosquitto_sub -v -t '#' -h 127.0.0.1
	//test       mosquitto_pub -h 127.0.0.1 -t 'topic/ingress' -m 'hello'
	
	public static void main( String[] args ) {
		GreenRuntime.run(new MQTTApp());
    }
		
	@Override
	public void declareConfiguration(GreenFramework builder) {

		mqttConfig = builder.useMQTT("127.0.0.1", MQTTBridge.defaultPort, "my name")
							.cleanSession(true)
							.transmissionQoS(MQTTQoS.exactlyOnce)
							.subscriptionQoS(MQTTQoS.exactlyOnce) //TODO: do tests for will and retain
							.keepAliveSeconds(10); //TODO: test with 2 seconds or less to make pings go.
		
		builder.setTimerPulseRate(1000); //TODO: bump this up so we can test pings.
		builder.enableTelemetry(); //TODO: we see no MQTT in the graph..
				
	}

	@Override
	public void declareBehavior(final GreenRuntime runtime) {
				
		runtime.bridgeSubscription("topic/ingress", mqttConfig); //optional 2 topics, optional transform lambda
		runtime.bridgeTransmission("topic/egress", mqttConfig); //optional 2 topics, optional transform lambda
		
		final MsgCommandChannel cmdChnl = runtime.newCommandChannel();	
		final PubSubService pubSubService = cmdChnl.newPubSubService();
		
		TimeListener timeListener = new TimeListener() {
			@Override
			public void timeEvent(long time, int iteration) {
				Writable writable = new Writable() {
					@Override
					public void write(ChannelWriter writer) {	
						Date d =new Date(System.currentTimeMillis());
						
						System.err.println("sent "+d);
						writer.writeUTF8Text("egress body "+d);

					}
				};
				pubSubService.publishTopic("topic/egress", writable);
			}
		};
		runtime.addTimePulseListener(timeListener);
		
		
		final MsgCommandChannel cmd = runtime.newCommandChannel();
		final PubSubService pubSubService2 = cmd.newPubSubService();
		
		PubSubListener listener = new PubSubListener() {
			
			
			@Override
			public boolean message(CharSequence topic, ChannelReader payload) {
				
				System.out.print("\ningress body: ");
				payload.readUTFOfLength(payload.available(), System.out);
				System.out.println();
				
				Writable writable = new Writable() {

					@Override
					public void write(ChannelWriter writer) {
						
						writer.writeUTF("second step test message");

					}
					
				};
				pubSubService2.publishTopic("localtest", writable);
				
				return true;
			}
		};
		runtime.addPubSubListener(listener ).addSubscription("topic/ingress");
			
		PubSubListener localTest = new PubSubListener() {
			@Override
			public boolean message(CharSequence topic, ChannelReader payload) {
				
				System.out.println("got topic "+topic+" payload "+payload.readUTF());
				
				return true;
			}			
		};
		runtime.addPubSubListener(localTest ).addSubscription("localtest");
		
		
	}
}
