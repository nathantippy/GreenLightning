package com.javanut.gl.impl.blocking;

import com.javanut.pronghorn.pipe.ChannelReader;

public interface TargetSelector {

	int pickTargetIdx(ChannelReader p);

}
