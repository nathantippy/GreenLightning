package com.javanut.gl.api;

import com.javanut.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.javanut.pronghorn.pipe.MessageSchema;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeConfig;
import com.javanut.pronghorn.stage.route.ReplicatorStage;
import com.javanut.pronghorn.stage.scheduling.GraphManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Grouper {

	private final Pipe[] inputPipes;
	private final Pipe[][] groupedPipes;
	private int count;
	
	private final static Logger logger = LoggerFactory.getLogger(Grouper.class);
	
	private Pipe[] first; //these will be the ones for the behavior.
	
	public Grouper(Pipe[] catagories) {
		this.inputPipes = catagories;
		this.groupedPipes = new Pipe[catagories.length][0];
	}

	/**
	 *
	 * @return count
	 */
	public int additions() {
		return count;
	}

	/**
	 *
	 * @param schema MessageSchema arg to used to check if there is a match
	 * @return null if no match found <p> else inputPipes[i].config()
	 */
	public PipeConfig config(MessageSchema schema) {
		int i = inputPipes.length;
		
		while (--i >= 0) {
			if (Pipe.isForSchema(inputPipes[i], schema)) {
				return inputPipes[i].config();
			}
		}
		//may not find a match, if not return null.
		return null;
	}

	/**
	 *
	 * @param pipes
	 */
	public void add(Pipe[] pipes) {

		if (0==count) {
			first = pipes;
		}
		count++;
		int i = pipes.length;
		while (--i>=0) {
			Pipe p = pipes[i];
			int j = inputPipes.length;
			while (--j>=0) {
				if (Pipe.isForSameSchema(inputPipes[j], p)) {
			      
					Pipe[] targetArray = groupedPipes[j];
					Pipe[] newArray = new Pipe[targetArray.length+1];
					System.arraycopy(targetArray, 0, newArray, 0, targetArray.length);
					newArray[targetArray.length] = p;
					groupedPipes[j] = newArray;
					return;
				}
			}
		}
	}
	
	
	public Pipe[] firstArray() {
		return first;
	}
	public void buildReplicators(GraphManager gm, ArrayList<ReactiveManagerPipeConsumer> consumers) {
		int i = inputPipes.length;
		while (--i>=0) {
			if (1 == groupedPipes[i].length) {
				//swap back to using direct connection
				int c = consumers.size();				
				while (--c>=0) {
					if (consumers.get(c).swapIfFound(groupedPipes[i][0], inputPipes[i])) {
						break;
					}
				}
				if (c<0) {
					//can not optimize this case so just add the extra hop.
					logger.info("internal error unable to find this pipe!  Hello Guys!");
					ReplicatorStage.newInstance(gm, inputPipes[i], groupedPipes[i]);
				}
				
			} else {
				ReplicatorStage.newInstance(gm, inputPipes[i], groupedPipes[i]);
			}
		}
	}
	

}
