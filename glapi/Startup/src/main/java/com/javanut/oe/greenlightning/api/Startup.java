package com.javanut.oe.greenlightning.api;


import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class Startup implements GreenApp
{
	
	private final AppendableProxy console;
	
	public Startup(Appendable console) {
		this.console = Appendables.proxy(console);
	}
	
	
    @Override
    public void declareConfiguration(GreenFramework c) {

    }

    @Override
    public void declareBehavior(GreenRuntime runtime) {

    	runtime.addStartupListener(()->{
    		console.append("Hello, this message will display once at start\n");
    		//now we shutdown the app
    		runtime.shutdownRuntime();
    	});
    	
    	
    }
}
