package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;


public class PubSub implements GreenApp
{
	private final AppendableProxy target;
	private final int seed;
	
	public PubSub(Appendable target, int seed) {
		this.target = Appendables.proxy(target);
		this.seed = seed;
	}
	
    @Override
    public void declareConfiguration(GreenFramework c) {
        //no connections are needed
    	//c.enableTelemetry();
    }

    @Override
    public void declareBehavior(GreenRuntime runtime) {

    	runtime.addStartupListener(new KickoffBehavior(runtime, target));
    	
    	runtime.addPubSubListener(new GenerateBehavior(runtime, "Count", target, seed))
    	                     .addSubscription("Next");
    	
    	CountBehavior counter = new CountBehavior(runtime, "Next");
		runtime.registerListener(counter)
		           			.addSubscription("Count", counter::triggerNextAndCount)
		           			.addSubscription("AnExample", counter::anotherMessage);
    	
    	
    	
    }
          
}
