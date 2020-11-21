package com.javanut.gl.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.impl.PubSubMethodListenerBase;
import com.javanut.gl.impl.schema.IngressMessages;
import com.javanut.gl.impl.schema.MessagePrivate;
import com.javanut.gl.impl.schema.MessagePubSub;
import com.javanut.gl.impl.schema.MessageSubscription;
import com.javanut.pronghorn.pipe.DataOutputBlobWriter;
import com.javanut.pronghorn.pipe.FieldReferenceOffsetManager;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeWriter;
import com.javanut.pronghorn.pipe.Writable;

public class PubSubFixedTopicService {

	private static final Logger logger = LoggerFactory.getLogger(PubSubService.class);
	private final MsgCommandChannel<?> msgCommandChannel;
	private final String topic;
    private final byte[] topicBytes;
    private int topicToken = -2; //lazy loaded by design
	
	
	public PubSubFixedTopicService(MsgCommandChannel<?> msgCommandChannel, String baseTopic, String trackTopic) {
	
		msgCommandChannel.initFeatures |= MsgCommandChannel.DYNAMIC_MESSAGING;
		this.msgCommandChannel = msgCommandChannel;
		this.topic = trackTopic;
		this.topicBytes = topic.getBytes();
		
		msgCommandChannel.builder.possiblePrivateTopicProducer(msgCommandChannel, baseTopic, msgCommandChannel.parallelInstanceId);

	}
	
	public PubSubFixedTopicService(MsgCommandChannel<?> msgCommandChannel, String baseTopic, String trackTopic,
								   int queueLength, int maxMessageSize) {
		
		msgCommandChannel.initFeatures |= MsgCommandChannel.DYNAMIC_MESSAGING;  
		this.msgCommandChannel = msgCommandChannel;
		this.topic = trackTopic;
		this.topicBytes = topic.getBytes();
		
		MsgCommandChannel.growCommandCountRoom(msgCommandChannel, queueLength);
		
		//NOTE: must set private topics in case we choose this
		msgCommandChannel.pcm.ensureSize(MessagePrivate.class, queueLength, maxMessageSize);
		
		//NOTE: must set public topics in case we choose this
		msgCommandChannel.pcm.ensureSize(MessagePubSub.class, queueLength, maxMessageSize);
		
		//also ensure consumers have pipes which can consume this.    		
		msgCommandChannel.pcm.ensureSize(MessageSubscription.class, queueLength, maxMessageSize);
		
		//IngressMessages Confirm that MQTT ingress is big enough as well			
		msgCommandChannel.pcm.ensureSize(IngressMessages.class, queueLength, maxMessageSize);
		
		msgCommandChannel.builder.possiblePrivateTopicProducer(msgCommandChannel, baseTopic, msgCommandChannel.parallelInstanceId);
	
	}
	
	
	public void logTelemetrySnapshot() {
		msgCommandChannel.logTelemetrySnapshot();
	}


	private final int token() {
		if (-2 == topicToken) {
			topicToken = computeToken();
		}
		return topicToken;
	}

	private int computeToken() {
		return null == msgCommandChannel.publishPrivateTopics ? -1 : msgCommandChannel.publishPrivateTopics.getToken(topicBytes, 0, topicBytes.length);
	}

	
	/**
	 * A method to determine if there is enough room in the pipe for more data
	 * @param messageCount int arg used in FieldReferenceOffsetManager.maxFragmentSize
	 * @return null==msgCommandChannel.goPipe || Pipe.hasRoomForWrite(msgCommandChannel.goPipe, FieldReferenceOffsetManager.maxFragmentSize(Pipe.from(msgCommandChannel.goPipe))*messageCount)
	 */
	public boolean hasRoomFor(int messageCount) {
		
		int token = token();
		if (token>=0) {
			//private topics use their own pipe which must be checked.
			return Pipe.hasRoomForWrite(msgCommandChannel.publishPrivateTopics.getPipe(token), messageCount * Pipe.sizeOf(MessagePrivate.instance, MessagePrivate.MSG_PUBLISH_1));
		}
		
		return 
				(null==msgCommandChannel.goPipe || Pipe.hasRoomForWrite(msgCommandChannel.goPipe, FieldReferenceOffsetManager.maxFragmentSize(Pipe.from(msgCommandChannel.goPipe))*messageCount))
				&&
			    (Pipe.hasRoomForWrite(msgCommandChannel.messagePubSub, FieldReferenceOffsetManager.maxFragmentSize(Pipe.from(msgCommandChannel.messagePubSub))*messageCount));
		
	}


    /**
     * Publishes specified failable topic with data written onto this channel
     * @param writable to write data into this channel
     * @return result if msgCommandChannel.goHasRoom else FailableWrite.Retry
     */
	public FailableWrite publishFailableTopic(FailableWritable writable) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		assert(writable != null);
		
		int token =  token();
		
		if (token>=0) {
			return msgCommandChannel.publishFailableOnPrivateTopic(token, writable);
		} else {
			if (msgCommandChannel.goHasRoom() && PipeWriter.hasRoomForWrite(msgCommandChannel.messagePubSub)) {
				PubSubWriter pw = (PubSubWriter) Pipe.outputStream(msgCommandChannel.messagePubSub);
		
				DataOutputBlobWriter.openField(pw);
				FailableWrite result = writable.write(pw);
		
				if (result == FailableWrite.Cancel) {
					msgCommandChannel.messagePubSub.closeBlobFieldWrite();
				}
				else {
					PipeWriter.presumeWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103);
					PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, WaitFor.All.policy());
					
		    		DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
		    		output.openField();	    		
		    		output.write(topicBytes, 0, topicBytes.length);
		    		
		    		MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
		    		
		    		output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
										
					//OLD PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);
		
					DataOutputBlobWriter.closeHighLevelField(pw, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
		
					PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
					MsgCommandChannel.publishGo(1, msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
				}
				return result;
			} else {
				return FailableWrite.Retry;
			}
		}
	}

	/**
     * Publishes specified failable topic with data written onto this channel and waits for success or failure
     * @param writable to write data into this channel
	 * @param ap WaitFor arg used in PipeWriter.writeInt
	 * @return result if msgCommandChannel.goHasRoom else FailableWrite.Retry
	 */
	public FailableWrite publishFailableTopic(FailableWritable writable, WaitFor ap) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		assert(writable != null);
		
		int token =  token();
		
		if (token>=0) {
			return msgCommandChannel.publishFailableOnPrivateTopic(token, writable);
		} else {
			if (msgCommandChannel.goHasRoom() 
				&& PipeWriter.hasRoomForWrite(msgCommandChannel.messagePubSub)) {
				PubSubWriter pw = (PubSubWriter) Pipe.outputStream(msgCommandChannel.messagePubSub);
		
				DataOutputBlobWriter.openField(pw);
				FailableWrite result = writable.write(pw);
		
				if (result == FailableWrite.Cancel) {
					msgCommandChannel.messagePubSub.closeBlobFieldWrite();
				}
				else {
					PipeWriter.presumeWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103);
					PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, ap.policy());
					
		    		DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
		    		output.openField();	    		
		    		output.write(topicBytes, 0, topicBytes.length);
		    		
		    		MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
		    		
		    		output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
										
					//OLD PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);
		
					DataOutputBlobWriter.closeHighLevelField(pw, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
		
					PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
					MsgCommandChannel.publishGo(1, msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
				}
				return result;
			} else {
				return FailableWrite.Retry;
			}
		}		
	}


	/**
     * Publishes specified topic with no data onto this channel
     * @return published topic if msgCommandChannel.goHasRoom
     */
	public boolean publishTopic() {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		
		int token =  token();
		
		if (token>=0) {
			return msgCommandChannel.publishOnPrivateTopic(token);
		} else {
			if (null==msgCommandChannel.messagePubSub) {
				if (msgCommandChannel.builder.isAllPrivateTopics()) {
					throw new RuntimeException("Discovered non private topic '"+topic+"' but exclusive use of private topics was set on.");
				} else {
					throw new RuntimeException("Enable DYNAMIC_MESSAGING for this CommandChannel before publishing.");
				}
			}
			
		    if (msgCommandChannel.goHasRoom()  && 
		    	PipeWriter.tryWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
				
				PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, WaitFor.All.policy());
		    	
				DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
				output.openField();	    		
				output.write(topicBytes, 0, topicBytes.length);
				
				MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
				
				output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
				
				////OLD: PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);         
		
		    	
		    	
				PipeWriter.writeSpecialBytesPosAndLen(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3, -1, 0);
				PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
		        MsgCommandChannel.publishGo(1,msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
		                    
		        return true;
		        
		    } else {
		        return false;
		    }
		}
	}

	/**
     * Publishes specified topic with no data onto this channel while not accepting new messages until published message is received
	 * @param waitFor WaitFor arg used in PipeWriter.writeInt
     * @return published topic if msgCommandChannel.goHasRoom
     */
	public boolean publishTopic(WaitFor waitFor) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		
		int token =  token();
		
		if (token>=0) {
			return msgCommandChannel.publishOnPrivateTopic(token);
		} else {
			if (null==msgCommandChannel.messagePubSub) {
				if (msgCommandChannel.builder.isAllPrivateTopics()) {
					throw new RuntimeException("Discovered non private topic '"+topic+"' but exclusive use of private topics was set on.");
				} else {
					throw new RuntimeException("Enable DYNAMIC_MESSAGING for this CommandChannel before publishing.");
				}
			}
			
		    if (msgCommandChannel.goHasRoom()  && 
		    	PipeWriter.tryWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
				
				PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, waitFor.policy());
		    	
				DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
				output.openField();	    		
				output.write(topicBytes, 0, topicBytes.length);
				
				MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
				
				output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
				
				////OLD: PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);         
		
		    	
		    	
				PipeWriter.writeSpecialBytesPosAndLen(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3, -1, 0);
				PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
		        MsgCommandChannel.publishGo(1,msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
		                    
		        return true;
		        
		    } else {
		        return false;
		    }
		}
	}

	/**
	 * Publishes specified topic with data written onto this channel
     * @param writable to write data into this channel
	 * @return published topic if token GTE 0
	 */
	public boolean publishTopic(Writable writable) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		assert(writable != null);
		
		int token = token();		
		
		if (token >= 0) {
			return msgCommandChannel.publishOnPrivateTopic(token, writable);
		} else {
			assert(!msgCommandChannel.builder.isAllPrivateTopics()) : "Internal error, useAllPrivate topics is set yet we found a non private topic.";
			
			//if messagePubSub is null then this is a private topic but why is publishPrivateTopics null?
			assert(null != msgCommandChannel.messagePubSub) : "pipe must not be null, topic: "+topic+" has privateTopicsPub:"+msgCommandChannel.publishPrivateTopics+"\n   cmd: "+msgCommandChannel.hashCode();
		    
			if (msgCommandChannel.goHasRoom()  && 
		    	PipeWriter.tryWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
				
				PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, WaitFor.All.policy());
		    	//PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);         
		
		    	DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
		 		output.openField();	    		
		 		output.write(topicBytes, 0, topicBytes.length);     		
		 		MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
		 		output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
		 		
		        PubSubWriter pw = (PubSubWriter) Pipe.outputStream(msgCommandChannel.messagePubSub);	           
		    	DataOutputBlobWriter.openField(pw);
		    	writable.write(pw);
		        DataOutputBlobWriter.closeHighLevelField(pw, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
		        
		        PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
		        MsgCommandChannel.publishGo(1,msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
		                    
		        
		        return true;
		        
		    } else {
		        return false;
		    }
		}
	}

	/**
     * Publishes specified topic with data written onto this channel while not accepting new messages until published message is received
     * @param writable to write data into this channel
	 * @param waitFor waitFor arg used in PipeWriter.writeInt
	 * @return published topic if token GTE 0
	 */
	public boolean publishTopic(Writable writable, WaitFor waitFor) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		assert(writable != null);
		
		int token = token();
		
		if (token>=0) {
			return msgCommandChannel.publishOnPrivateTopic(token, writable);
		} else {
		    if (msgCommandChannel.goHasRoom() && 
		    	PipeWriter.tryWriteFragment(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103)) {
				
				PipeWriter.writeInt(msgCommandChannel.messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_QOS_5, waitFor.policy());
		    	//PipeWriter.writeUTF8(messagePubSub, MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1, topic);         
		
		    	DataOutputBlobWriter<MessagePubSub> output = PipeWriter.outputStream(msgCommandChannel.messagePubSub);
		 		output.openField();	    		
		 		output.write(topicBytes, 0, topicBytes.length);     		
		 		MsgCommandChannel.publicTrackedTopicSuffix(msgCommandChannel, output);
		 		output.closeHighLevelField(MessagePubSub.MSG_PUBLISH_103_FIELD_TOPIC_1);
		 		
		        PubSubWriter pw = (PubSubWriter) Pipe.outputStream(msgCommandChannel.messagePubSub);	           
		    	DataOutputBlobWriter.openField(pw);
		    	writable.write(pw);
		        DataOutputBlobWriter.closeHighLevelField(pw, MessagePubSub.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
		        
		        PipeWriter.publishWrites(msgCommandChannel.messagePubSub);
		
		        MsgCommandChannel.publishGo(1, msgCommandChannel.builder.pubSubIndex(), msgCommandChannel);
		                    
		        
		        return true;
		        
		    } else {
		        return false;
		    }
		}
	}
		
	public void presumePublishTopic(Writable writable) {
		presumePublishTopic(writable, WaitFor.All);
	}

	public void presumePublishTopic(Writable writable, WaitFor waitFor) {
		assert((0 != (msgCommandChannel.initFeatures & MsgCommandChannel.DYNAMIC_MESSAGING))) : "CommandChannel must be created with DYNAMIC_MESSAGING flag";
		
		if (!publishTopic(writable, waitFor)) {
			logger.warn("unable to publish on topic {} must wait.",topic);
			while (!publishTopic(writable, waitFor)) {
				Thread.yield();
			}
		}
	}


	
	/**
	 * start shutdown of the runtime, this can be vetoed or postponed by any shutdown listeners
	 */
	public void requestShutdown() {
		
		assert(msgCommandChannel.enterBlockOk()) : "Concurrent usage error, ensure this never called concurrently";
		try {
			msgCommandChannel.builder.requestShutdown();
		} finally {
		    assert(msgCommandChannel.exitBlockOk()) : "Concurrent usage error, ensure this never called concurrently";      
		}
	}
	
}
