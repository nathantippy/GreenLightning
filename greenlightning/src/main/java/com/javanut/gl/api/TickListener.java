package com.javanut.gl.api;

import com.javanut.gl.impl.TickListenerBase;

/**
 * Functional interface for a listener for ticks every time the run in reactive listener is processed
 * by the {@link MsgRuntime}.
 *
 * @author Nathan Tippy
 */
@FunctionalInterface
public interface TickListener extends Behavior, TickListenerBase {

}
