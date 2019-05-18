package com.javanut.gl.impl.stage;

import com.javanut.pronghorn.pipe.Pipe;

public abstract class ReactiveOperator {
	
	public abstract void apply(int index, Object target, Pipe input, ReactiveListenerStage operatorImpl);
	
}


