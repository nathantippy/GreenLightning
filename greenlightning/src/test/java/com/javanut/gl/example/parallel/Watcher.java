package com.javanut.gl.example.parallel;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.PubSubListener;
import com.javanut.pronghorn.pipe.ChannelReader;

public class Watcher implements PubSubListener {

	public Watcher(GreenRuntime runtime) {
		
	}

	@Override
	public boolean message(CharSequence topic, ChannelReader payload) {
		
		System.out.println("got message topic: "+topic);
	
		return true;
	}

}
