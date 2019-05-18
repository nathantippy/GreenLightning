package com.javanut.oe.greenlightning.api;

import java.util.Random;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class GenerateBehavior implements PubSubListener {

	private Random rand;
	private final PubSubFixedTopicService channel;
	private final AppendableProxy target;
	
	public GenerateBehavior(GreenRuntime runtime, CharSequence publishTopic, AppendableProxy target, int seed) {
		
		this.target = target;
		this.channel = runtime.newCommandChannel().newPubSubService(publishTopic.toString());
		
		this.rand = new Random(seed);
	}


	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		//Note if this behavior is subscribed to more than 1 topic we will need
		//to branch here based on the value of topic.
		
		Appendables.appendValue(target, rand.nextInt(101)).append(' ');		
		return channel.publishTopic();		
	
	}

}
