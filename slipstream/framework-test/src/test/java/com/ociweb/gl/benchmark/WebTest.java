package com.ociweb.gl.benchmark;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.javanut.gl.api.GreenRuntime;
import com.javanut.gl.api.MsgRuntime;
import com.javanut.gl.test.LoadTester;
import com.javanut.pronghorn.network.ClientSocketReaderStage;
import com.javanut.pronghorn.stage.scheduling.GraphManager;
import com.javanut.pronghorn.util.Appendables;


public class WebTest {
	
	final static boolean useTLS = false;
	final static int timeoutMS = 600_000;
	final static int totalCalls = 2_000;
	
	private static FrameworkTest app;
	private static GreenRuntime runtime;
	
	static int port = (int) (3000 + (System.nanoTime()%12000));
	static String host = "127.0.0.1";
	
	
	static int telemetryPort = 8097;
	static boolean telemetry = false;
	
//	@BeforeClass
//	public static void startServer() {
//
//		GraphManager.showThreadIdOnTelemetry = true;
//		ClientSocketReaderStage.abandonSlowConnections = false;//allow tester to wait for responses.
//				
//		System.setProperty("greenlightning.tracks.max", "1");
//		app = new FrameworkTest("127.0.0.1", port, 1, 128, 1<<15, -1,
//				                  null, null, null, null);
//		runtime = GreenRuntime.run(app);	
//		
//	}
//		
//	@AfterClass
//	public static void stopServer() {
//		if (null != runtime) {
//			runtime.shutdownRuntime();	
//			runtime = null;
//		}
//	}

	
	@Test
	public void dummy() {
		assertTrue(true);
	}
	
	@Ignore
	public void plaintextTest() {
			
		
			    //ServerSocketWriterStage.showWrites = true;
		
				int inFlightBits = 8; 
				int tracks = 1;
				int callsPerTrack = totalCalls/tracks; 
				boolean testTelemetry = false;
		
				StringBuilder uploadConsoleCapture = new StringBuilder();
				LoadTester.runClient(
						null,
						(i,r) -> r.statusCode()==200 , 
						"/plaintext", 
						useTLS, testTelemetry, 
						tracks, callsPerTrack, 
						host, port, timeoutMS, inFlightBits,
						MsgRuntime.getGraphManager(runtime),						
						Appendables.join(uploadConsoleCapture,System.out));	
				
				assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(callsPerTrack*tracks))>=0);

				
	}

	
	@Ignore
	public void jsonTest() {
		
				int inFlightBits = 8;
				int tracks = 1;
				int callsPerTrack = totalCalls/tracks; 
				boolean testTelemetry = false;
		
				StringBuilder uploadConsoleCapture = new StringBuilder();
				LoadTester.runClient(
						null,
						(i,r) -> r.statusCode()==200 , 
						"/json", 
						useTLS, testTelemetry, 
						tracks, callsPerTrack, 
						host, port, timeoutMS, inFlightBits,
						MsgRuntime.getGraphManager(runtime),						
						Appendables.join(uploadConsoleCapture,System.out));	
				
				assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(callsPerTrack*tracks))>=0);
		
	}
	
	
	@Ignore
	public void queryTest() {		
		if (app.foundDB.get()) {			
				int totalCalls = 2_000;
				int inFlightBits = 8;
				int tracks = 1;
				int callsPerTrack = totalCalls/tracks; 
				boolean testTelemetry = false;
		
				StringBuilder uploadConsoleCapture = new StringBuilder();
				LoadTester.runClient(
						null,
						(i,r) -> r.statusCode()==200, 
						"/queries?queries=40", 
						useTLS, testTelemetry, 
						tracks, callsPerTrack, 
						host, port, timeoutMS, inFlightBits,
						MsgRuntime.getGraphManager(runtime),						
						Appendables.join(uploadConsoleCapture,System.out));	
				
				assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(callsPerTrack*tracks))>=0);
		} else {
			System.out.println("DB testing skipped. No DB");
			assertTrue(true);//no DB to test with
		}
	}
	
	
	@Ignore
	public void updatesTest() {
		if (app.foundDB.get()) {
	
				int inFlightBits = 8;
				int tracks = 1;
				int callsPerTrack = totalCalls/tracks; 
				boolean testTelemetry = false;
		
				StringBuilder uploadConsoleCapture = new StringBuilder();
				LoadTester.runClient(
						null,
						(i,r) -> r.statusCode()==200, 
						"/updates?queries=40", 
						useTLS, testTelemetry, 
						tracks, callsPerTrack, 
						host, port, timeoutMS, inFlightBits,
						MsgRuntime.getGraphManager(runtime),						
						Appendables.join(uploadConsoleCapture,System.out));	
				
				assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(callsPerTrack*tracks))>=0);
		} else {
				System.out.println("DB testing skipped. No DB");
				assertTrue(true);//no DB to test with
		}
	}
	
	@Ignore
	public void fortunesTest() {
		if (app.foundDB.get()) {
	
				int totalCalls = 2_000;
				int inFlightBits = 8;
				int tracks = 1;
				int callsPerTrack = totalCalls/tracks; 
				boolean testTelemetry = false;
		
				StringBuilder uploadConsoleCapture = new StringBuilder();
				LoadTester.runClient(
						null,
						(i,r) -> r.statusCode()==200, 
						"/fortunes", 
						useTLS, testTelemetry, 
						tracks, callsPerTrack, 
						host, port, timeoutMS, inFlightBits,
						MsgRuntime.getGraphManager(runtime),						
						Appendables.join(uploadConsoleCapture,System.out));	
				
				assertTrue(uploadConsoleCapture.toString(), uploadConsoleCapture.indexOf("Responses invalid: 0 out of "+(callsPerTrack*tracks))>=0);
		} else {
				System.out.println("DB testing skipped. No DB");
				assertTrue(true);//no DB to test with
		}
	}	
	
	
	
    
}
