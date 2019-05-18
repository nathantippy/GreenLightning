package com.javanut.gl.impl;

import com.javanut.gl.api.Behavior;
import com.javanut.gl.api.MsgRuntime;

/**
 * Functional interface for changes in a state machine registered with the
 * {@link MsgRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface StateChangeListenerBase <E extends Enum<E>>{
	/**
	 * Invoked when a state machine registered with the {@link MsgRuntime}
	 * changes state.
	 *
	 * @param oldState Old state of the state machine.
	 * @param newState New state of the state machine.
	 */
	boolean stateChange(E oldState, E newState);
}
