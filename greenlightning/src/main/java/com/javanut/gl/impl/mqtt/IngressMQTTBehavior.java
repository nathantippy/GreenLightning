package com.javanut.gl.impl.mqtt;

import com.javanut.gl.api.MQTTConnectionStatus;
import com.javanut.gl.api.MsgCommandChannel;
import com.javanut.gl.api.MsgRuntime;
import com.javanut.gl.api.PubSubFixedTopicService;
import com.javanut.gl.api.TickListener;
import com.javanut.gl.impl.stage.IngressConverter;
import com.javanut.pronghorn.network.schema.MQTTClientResponseSchema;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeConfigManager;
import com.javanut.pronghorn.util.TrieParser;
import com.javanut.pronghorn.util.TrieParserReaderLocal;

public class IngressMQTTBehavior implements TickListener {
	
	private final CharSequence[] externalTopicsSub;
	private final CharSequence[] internalTopicsSub;
	private final IngressConverter[] convertersSub;
	private final Pipe<MQTTClientResponseSchema> responsePipe;

	private final PubSubFixedTopicService[] pubSubService;
	private final PubSubFixedTopicService conFeedbackService;
	
	private final TrieParser externalTopicTrie;
	
	//TODO: can the IngressConverter be used to parse JSON and do other tasks??
	
	public IngressMQTTBehavior(MsgRuntime<?,?,?> msgRuntime, 
								CharSequence[] externalTopicsSub,
								CharSequence[] internalTopicsSub, 
								IngressConverter[] convertersSub, 
								CharSequence connectionFeedbackTopic,
								Pipe<MQTTClientResponseSchema> clientResponse) {
		
		this.externalTopicsSub = externalTopicsSub;
		this.internalTopicsSub = internalTopicsSub;
		this.convertersSub = convertersSub;
		this.responsePipe = clientResponse;
		
		assert(externalTopicsSub.length == internalTopicsSub.length);
		assert(convertersSub.length == internalTopicsSub.length);
		
      	PipeConfigManager pcm = new PipeConfigManager(4, MsgRuntime.defaultCommandChannelLength, MsgRuntime.defaultCommandChannelMaxPayload);
      	MsgCommandChannel cmd = MsgRuntime.builder(msgRuntime).newCommandChannel(-1,  pcm);  
      	
      	//one service per each internal topic, this allows each to become "private" when possible
      	PubSubFixedTopicService[] targetTopics = new PubSubFixedTopicService[internalTopicsSub.length];
      	int i = targetTopics.length;
      	while (--i>=0) {
      		targetTopics[i] = cmd.newPubSubService(internalTopicsSub[i].toString());
      	}
      	pubSubService = targetTopics;	
      	if (null == connectionFeedbackTopic) {
      		conFeedbackService = null;
      	} else {
      		int j = targetTopics.length;
      	
      		while (--j>=0 && (!internalTopicsSub[j].equals(connectionFeedbackTopic))) {      			
      		}
      		if (j>=0) {
      			//do not create extra service if not needed because it will block the private topic logic
      			conFeedbackService = targetTopics[j];
      		} else {
      			conFeedbackService = cmd.newPubSubService(connectionFeedbackTopic.toString());
      		}
      	}
      	int j = externalTopicsSub.length;
      	externalTopicTrie = new TrieParser(j*20,2,false,false,false);
      	while (--j>=0) {
      		externalTopicTrie.setUTF8Value(externalTopicsSub[j], j);
      	}
	}

	@Override
	public void tickEvent() {
		
		while (Pipe.hasContentToRead(responsePipe)) {
			
			if (Pipe.peekMsg(responsePipe, MQTTClientResponseSchema.MSG_MESSAGE_3)) {
				DataInputBlobReader<MQTTClientResponseSchema> topicIn = Pipe.peekInputStream(responsePipe, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_TOPIC_23);

				int topicIdx = (int)topicIn.parse(TrieParserReaderLocal.get(), externalTopicTrie, topicIn.available());
				
				if (pubSubService[topicIdx].hasRoomFor(1)) {
					
					int idx = Pipe.takeMsgIdx(responsePipe);
					
					//we are now free to consume the actual message off the pipe...
					int qos = Pipe.takeInt(responsePipe);
					int retail = Pipe.takeInt(responsePipe);
					int dup = Pipe.takeInt(responsePipe);
									
					ChannelReader topic = Pipe.openInputStream(responsePipe); //topic we already know					
					
					ChannelReader payload = Pipe.openInputStream(responsePipe); //payload
					
					pubSubService[topicIdx].publishTopic(w->{
						convertersSub[topicIdx].convertData(payload, w);
					});

					Pipe.confirmLowLevelRead(responsePipe, Pipe.sizeOf(responsePipe, idx));
					Pipe.releaseReadLock(responsePipe);	
					
				} else {
					//come back later, we have no room now.
					break;
				}
				
			} else {
				int idx = Pipe.takeMsgIdx(responsePipe);
				
				if (null==conFeedbackService || conFeedbackService.hasRoomFor(1)) {
					
					if (MQTTClientResponseSchema.MSG_CONNECTIONATTEMPT_5 == idx) {					
						int resultCode = Pipe.takeInt(responsePipe);
						int sessionPresent = Pipe.takeInt(responsePipe);
						//send to ...
						if (null!=conFeedbackService) {
							conFeedbackService.publishTopic((w)-> {
								w.writeInt(MQTTConnectionStatus.fromSpecification(resultCode).getSpecification());
								w.writeBoolean(sessionPresent != 0);
							});
						}
						//
						Pipe.confirmLowLevelRead(responsePipe, Pipe.sizeOf(responsePipe, idx));
						Pipe.releaseReadLock(responsePipe);						
						
					} else if (MQTTClientResponseSchema.MSG_SUBSCRIPTIONRESULT_4 == idx) {
						Pipe.skipNextFragment(responsePipe, idx); //NOTE: we have max QOS here but do not know who to send it to..					
					} else {
						assert(-1 == idx) : "unexpected "+idx;
						Pipe.skipNextFragment(responsePipe, idx);
						if (null!=conFeedbackService) {
							conFeedbackService.requestShutdown();
						}
						break;
					}
				} else {
					break;
				}
			}
		}
	}
}
