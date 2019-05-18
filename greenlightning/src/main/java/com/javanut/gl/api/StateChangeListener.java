package com.javanut.gl.api;

import com.javanut.gl.impl.StateChangeListenerBase;

public interface StateChangeListener<E extends Enum<E>> extends Behavior, StateChangeListenerBase <E> {
}
