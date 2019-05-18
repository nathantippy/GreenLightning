package com.javanut.gl.api;

import com.javanut.gl.impl.BuilderImpl;
import com.javanut.pronghorn.pipe.PipeConfigManager;
import com.javanut.pronghorn.stage.scheduling.GraphManager;

public class GreenCommandChannel extends MsgCommandChannel<BuilderImpl> {

	/**
	 *
	 * @param builder arg of data type BuilderImpl
	 * @param features int arg
	 * @param parallelInstanceId int arg
	 * @param pcm arg of data type PipeConfigManager
	 */
	public GreenCommandChannel(BuilderImpl builder, int features, int parallelInstanceId,
			PipeConfigManager pcm) {
		super(builder, features, parallelInstanceId, pcm);
	}

}
