package com.javanut.gl.impl.stage;

import com.javanut.gl.impl.schema.TrafficOrderSchema;
import com.javanut.pronghorn.pipe.Pipe;

public abstract class ReactiveProxy {

	public abstract void startup();
	
	public abstract void run();
	
	public abstract void shutdown();

	public abstract int getFeatures(Pipe<TrafficOrderSchema> orderPipe);
		
}
