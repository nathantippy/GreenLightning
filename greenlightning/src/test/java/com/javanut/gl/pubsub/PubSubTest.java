package com.javanut.gl.pubsub;

import org.junit.Ignore;
import org.junit.Test;

import com.javanut.gl.api.GreenRuntime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PubSubTest {

	@Ignore
	public void wildcardTest() {

		StringBuilder collectedRoot = new StringBuilder();
		StringBuilder collectedGreen = new StringBuilder();

		boolean completed = GreenRuntime.testConcurrentUntilShutdownRequested(new WildExample(collectedRoot,collectedGreen),100);

		//System.err.println("Root:\n" + collectedRoot);
		//System.out.println("Green:\n" + collectedGreen);

		assertTrue(completed);
		assertEquals("root/green/color\nroot/green/frequency\n", collectedGreen.toString());
		
		//TODO: this part is broken 
		//     must investigate MessagePubSubStage private void addSubscription( method
		//TODO: also add tests to confirm /?/ single path part works.		
		//assertEquals("root/green/color\nroot/green/frequency\nroot/red/frequency\nroot/green/frequency\nroot/shutdown\n", collectedRoot.toString());

	}
}
