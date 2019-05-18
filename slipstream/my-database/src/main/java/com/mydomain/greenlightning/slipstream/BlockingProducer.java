package com.mydomain.greenlightning.slipstream;

import com.javanut.gl.impl.blocking.TargetSelector;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.stage.blocking.BlockingWorker;
import com.javanut.pronghorn.stage.blocking.BlockingWorkerProducer;


public class BlockingProducer implements BlockingWorkerProducer, TargetSelector {

	private final String dbURL;
	
	public BlockingProducer(String dbURL) {
		assert(null!=dbURL) : "URL can not be null";
		this.dbURL = dbURL;
	}	
	
	@Override
	public BlockingWorker newWorker() {		
		return new ExampleWorker(dbURL);
	}

	@Override
	public String name() {
		return "BlockingExample";
	}

	@Override
	public int pickTargetIdx(ChannelReader p) {
		
		return 0;
	}
	
}
