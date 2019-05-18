package com.javanut.gl.api;

import com.javanut.gl.impl.BuilderImpl;
import com.javanut.pronghorn.network.schema.HTTPRequestSchema;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeConfig;

public class ListenerConfig {


	/**
	 *
	 * @param builder BuilderImpl that implements appendPipeMappingIncludingGroupIds
	 * @param routes int[] used as arg in builder.appendPipeMappingIncludingGroupIds
	 * @param parallelInstance int used as arg in ListenerConfig.computeParallel and builder.appendPipeMappingIncludingGroupIds
	 * @param restRequests
	 */
	public static void recordPipeMapings(BuilderImpl builder, int[] routes, int parallelInstance,
			Pipe<HTTPRequestSchema>[] restRequests) {
		int r;
		int idx = restRequests.length;
		
		final int p = ListenerConfig.computeParallel(builder, parallelInstance);
		
		int x = p;
		while (--x>=0) {	
			int parallelId = -1 == parallelInstance ? x : parallelInstance;
			Pipe<HTTPRequestSchema> pipe = restRequests[--idx];
			builder.appendPipeMappingIncludingGroupIds(pipe, parallelId, routes);           
		}
	}

	/**
	 *
	 * @param builder BuilderImpl used to initialize pipe
	 * @param parallelInstance int used to initialize new Pipe array
	 * @return all the rest requests
	 */
	public static Pipe<HTTPRequestSchema>[] newHTTPRequestPipes(BuilderImpl builder, final int parallelInstance) {
		Pipe<HTTPRequestSchema>[] restRequests = new Pipe[parallelInstance];
				
		PipeConfig<HTTPRequestSchema> pipeConfig = builder.pcm.getConfig(HTTPRequestSchema.class).grow2x();
		int idx = restRequests.length;
		while (--idx>=0) {
			restRequests[idx] = builder.newHTTPRequestPipe(pipeConfig);
		}
		return restRequests;
	}

	/**
	 *
	 * @param builder BuilderImpl used to determine value of p
	 * @param parallelInstance int used to determine value of p
	 * @return p <p> p = builder.parallelTracks() if -1 == parallelInstance else p = 1
	 */
	public static int computeParallel(BuilderImpl builder, int parallelInstance) {
		final int p;
		if (-1 == parallelInstance) {
			p = builder.parallelTracks();
		} else {
			p = 1;
		}
		return p;
	}
	


}
