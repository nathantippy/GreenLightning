package com.javanut.gl.pubsub;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;

public class WildExample implements GreenApp {
	private final Appendable collectedRoot;
	private final Appendable collectedGreen;
	
	WildExample(Appendable collectedRoot, Appendable collectedGreen) {
		this.collectedRoot = collectedRoot;
		this.collectedGreen = collectedGreen;
	}
	
	@Override
	public void declareConfiguration(GreenFramework builder) {
		
	}

	@Override
	public void declareBehavior(GreenRuntime runtime) {
		runtime.addPubSubListener(new WildListener(collectedGreen, runtime)).addSubscription("root/green/#");
		runtime.addPubSubListener(new WildListener(collectedRoot, runtime)).addSubscription("root/#");
		runtime.addStartupListener(new WildPublish(runtime));
	}
}
