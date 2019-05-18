package com.javanut.gl.example.echo;

import org.junit.Ignore;
import org.junit.Test;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.test.ParallelClientLoadTester;
import com.javanut.gl.test.ParallelClientLoadTesterConfig;
import com.javanut.gl.test.ParallelClientLoadTesterPayload;
import com.javanut.pronghorn.network.ClientSocketWriterStage;

public class EchoTaskTest {
	
	
	@Ignore
	public void runIgore() {

		//ClientSocketWriterStage.showWrites = true;
		//ServerSocketReaderStage.showRequests = true;
		//HTTP1xRouterStage.showHeader = true;
		
		boolean telemetry = false;  //must not be true when checked in.
		int cyclesPerTrack = 10;//10000;
		int parallelTracks = 1;
		int timeoutMS = 600_000;
				
		ClientSocketWriterStage.showWrites = false;
		
		StringBuilder target = new StringBuilder();
		
		GreenRuntime.run(new EchoExampleApp(target));
		
		ParallelClientLoadTesterPayload payload = null;
				
		
		//TODO: tester needs to send headers and get headers back.
		
		ParallelClientLoadTesterConfig config2 = 
				new ParallelClientLoadTesterConfig(parallelTracks, 
						cyclesPerTrack, 6084, "/test", telemetry);
		
		config2.simultaneousRequestsPerTrackBits  = 0;
		config2.telemetryHost="127.0.0.1";
		
		//TODO: need client to confirm headers present.
		GreenRuntime.testConcurrentUntilShutdownRequested(
				new ParallelClientLoadTester(config2, payload),
				timeoutMS);

		System.out.println(target);
		//TODO: assert some things about these results.
	}
	
	
}
