package com.mydomain.greenlightning.slipstream;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.test.LoadTester;
import com.javanut.json.encode.JSONRenderer;
import com.javanut.pronghorn.network.ClientAbandonConnectionScanner;
import com.javanut.pronghorn.util.AppendableBuilder;
import com.javanut.pronghorn.util.Appendables;

public class DBWebTest {
	
	static GreenRuntime runtime;
	
	static int port = (int) (3000 + (System.nanoTime()%12000));
	
	static String host = "127.0.0.1";
	static int timeoutMS = 600_000; //10 minutes	
	static boolean telemetry = false;
	static boolean useTLS = true;
	
	@BeforeClass
	public static void startServer() {
		
		//2Min since we are on slow hardware
		ClientAbandonConnectionScanner.absoluteNSToKeep = 120_000_000_000L; 
		ClientAbandonConnectionScanner.absoluteNSToAbandon = 300_000_000_000L; //kill after 5 Min

		runtime = GreenRuntime.run(new MyDBMicroservice(useTLS, port, telemetry));
		
	}
		
	@AfterClass
	public static void stopServer() {
		if (null!=runtime) {
			runtime.shutdownRuntime();
			runtime = null;
		}
	}

	private JSONRenderer<Product> renderer = new JSONRenderer<Product>()
			.startObject()
			.integer(   "id", o->o.id)
			.integer(   "quantity", o->o.quantity)
			.string(    "name", (o,t)->t.append(o.name))
			.bool(      "disabled", o->o.disabled)
			.endObject();
	
	@Test
	public void uploadProductsTest() {

		int tracks = 1;
		int callsPerTrack = 100; 
		int inFlightBits = 3;
		boolean telemetry2 = false;

		StringBuilder uploadConsoleCapture = new StringBuilder();
		LoadTester.runClient(
				(i,w) -> renderer.render(w, new Product((int)i%10_000)) ,
				(i,r) -> r.statusCode()==200, 
				"/update", 
				useTLS, telemetry2, 
				tracks, callsPerTrack, 
				host, port, timeoutMS, inFlightBits, Appendables.join(uploadConsoleCapture,System.out));	
		
		assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(tracks*callsPerTrack))>=0);

		//////////////////////////////////////
		//now test that we get the values back
		//////////////////////////////////////
		
		tracks = 1; //this test depends on having sequential tests
		callsPerTrack = 1;
		
		StringBuilder captured = new StringBuilder();
		
		//we create this once and it is used for each call.
		final AppendableBuilder target = new AppendableBuilder(1000);	
		
		LoadTester.runClient(
			 null, 
			(i,r) -> {
					target.clear();
					renderer.render(target, new Product((int)i));						
					return  (200 == r.statusCode()) &&
							target.isEqual(r.structured().readPayload()); //equals is done without creating any String object
					
				  }, 
			(i) -> "/query?id="+i,
			useTLS, false, 
			tracks, callsPerTrack, 
			host, port, timeoutMS,
			captured);		

		 assertTrue(captured.toString(), captured.indexOf("Responses invalid: 0 out of "+(tracks*callsPerTrack))>=0);

	}
	
}
