package com.javanut.oe.greenlightning.api;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.util.AppendableProxy;
import com.javanut.pronghorn.util.Appendables;

public class StateMachine implements GreenApp
{

	public enum StopLight{
		
		Go("Green"), 
		Caution("Yellow"), 
		Stop("Red");
		
		private String color;
		
		StopLight(String lightColor){
			color = lightColor;
		}
		
		public String getColor(){
			return color;
		}
	}
	
	private final AppendableProxy console;
	private final int rate;
	
	public StateMachine(Appendable console, int rate) {
		this.console = Appendables.proxy(console);
		this.rate = rate;
	}
	
    @Override
    public void declareConfiguration(GreenFramework c) {
    	
    	c.startStateMachineWith(StopLight.Stop);
    	c.setTimerPulseRate(rate);
    }

	@Override
    public void declareBehavior(GreenRuntime runtime) {
        
        runtime.addTimePulseListener(new TimingBehavior(runtime, console));
		runtime.addStateChangeListener(new StateChangeBehavior(console))
		                     .includeStateChangeTo(StopLight.Go);
		runtime.addStateChangeListener(new StateChangeBehavior(console))
		                     .includeStateChangeTo(StopLight.Stop);
				
		
    }
          
}
