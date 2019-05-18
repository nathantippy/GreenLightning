package com.javanut.gl.api;

public interface DeclareBehavior<R extends MsgRuntime<?, ?, R>> {

	void declareBehavior(R runtime);
	
}
