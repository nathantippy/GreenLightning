package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class Timer implements GreenApp
{
	private final AppendableProxy console;
	private final int rate;
		
	public Timer(Appendable console, int rate) {
		this.console = Appendables.proxy(console);
		this.rate = rate;
	}
	
    @Override
    public void declareConfiguration(GreenFramework config) {
    	config.setTimerPulseRate(rate); //the rate at which time is checked in milliseconds 
    }

    @Override
    public void declareBehavior(GreenRuntime runtime) {
    	runtime.addTimePulseListener(new TimeBehavior(runtime, console));
    }
}
