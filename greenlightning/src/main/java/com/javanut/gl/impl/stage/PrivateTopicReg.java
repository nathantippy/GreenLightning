package com.javanut.gl.impl.stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.MsgCommandChannel;
import com.javanut.gl.impl.ChildClassScannerVisitor;
import com.javanut.gl.impl.schema.TrafficOrderSchema;
import com.javanut.pronghorn.pipe.Pipe;

class PrivateTopicReg implements ChildClassScannerVisitor<MsgCommandChannel> {

	private static final Logger logger = LoggerFactory.getLogger(PrivateTopicReg.class);
	private final ReactiveListenerStage<?> reactiveListenerStage;
	public PrivateTopicReg(ReactiveListenerStage<?> reactiveListenerStage) {
		this.reactiveListenerStage = reactiveListenerStage;
	}

	private Pipe<TrafficOrderSchema> target;
	   
    public void init(Pipe<TrafficOrderSchema> target) {
	   this.target = target;
    }

	public boolean visit(MsgCommandChannel cmdChnl, Object topParent, String name) {
		    MsgCommandChannel.setPrivateTopics(cmdChnl, reactiveListenerStage.publishPrivateTopics);	
			return true;//keep going
	   }

	
}
