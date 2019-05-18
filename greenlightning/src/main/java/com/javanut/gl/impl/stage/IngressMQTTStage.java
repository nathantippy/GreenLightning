package com.javanut.gl.impl.stage;

import static com.javanut.gl.impl.schema.IngressMessages.MSG_PUBLISH_103;
import static com.javanut.gl.impl.schema.IngressMessages.MSG_PUBLISH_103_FIELD_PAYLOAD_3;
import static com.javanut.gl.impl.schema.IngressMessages.MSG_PUBLISH_103_FIELD_TOPIC_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.MQTTConnectionFeedback;
import com.javanut.gl.api.MQTTConnectionStatus;
import com.javanut.gl.impl.schema.IngressMessages;
import com.javanut.pronghorn.network.schema.MQTTClientResponseSchema;
import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.ChannelWriter;
import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.pipe.DataOutputBlobWriter;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeReader;
import com.javanut.pronghorn.pipe.PipeWriter;
import com.javanut.pronghorn.stage.PronghornStage;
import com.javanut.pronghorn.stage.scheduling.GraphManager;

public class IngressMQTTStage extends PronghornStage {

	private final Pipe<MQTTClientResponseSchema> input;
	private final Pipe<IngressMessages> output;
	private final CharSequence[] externalTopic; 
	private final CharSequence[] internalTopic;
	private final IngressConverter[] converter;
	private final CharSequence connectionFeedbackTopic;
	private final MQTTConnectionFeedback connectResult = new MQTTConnectionFeedback();
	private boolean allTopicsMatch;
	private static final Logger logger = LoggerFactory.getLogger(IngressMQTTStage.class);
	
	public static final IngressConverter copyConverter = new IngressConverter() {		
		@Override
		public void convertData(ChannelReader inputStream, ChannelWriter outputStream) {			
			inputStream.readInto(outputStream, inputStream.available());
		}
	};
		
	public IngressMQTTStage(GraphManager graphManager, Pipe<MQTTClientResponseSchema> input, Pipe<IngressMessages> output, 
            CharSequence[] externalTopic, CharSequence[] internalTopic) {
		this(graphManager, input, output, externalTopic, internalTopic, asArray(copyConverter, internalTopic.length ), null);
	}
	
	public IngressMQTTStage(GraphManager graphManager, Pipe<MQTTClientResponseSchema> input, Pipe<IngressMessages> output,
							CharSequence[] externalTopic, CharSequence[] internalTopic, IngressConverter[] converter, CharSequence connectionFeedbackTopic) {
		
		super(graphManager, input, output);
		this.input = input;
		this.output = output;
		this.externalTopic = externalTopic;
		this.internalTopic = internalTopic;
		this.connectionFeedbackTopic = connectionFeedbackTopic;
		this.allTopicsMatch = isMatching(internalTopic,externalTopic,converter);
		this.converter = converter;
		
	}
	
	private static IngressConverter[] asArray(IngressConverter copyconverter, int length) {
		IngressConverter[] array = new IngressConverter[length];
		while (--length>=0) {
			array[length] = copyconverter;
		}
		return array;
	}

	private boolean isMatching(CharSequence[] internalTopic, CharSequence[] externalTopic, IngressConverter[] converter) {
		assert(internalTopic.length == externalTopic.length);
		int i = internalTopic.length;
		while (--i>=0) {
			CharSequence a = internalTopic[i];
			CharSequence b = externalTopic[i];
			if (a.length()!=b.length()) {
				return false;
			}
			int j = a.length();
			while (--j>=0) {
				if (a.charAt(j)!=b.charAt(j)) {
					return false;
				}	
			}
		}
		IngressConverter prototype = converter[0];
		int k = converter.length;
		while(--k>=0) {
			if (prototype!=converter[k]) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void run() {
	
		while (PipeWriter.hasRoomForWrite(output) &&
			   PipeReader.tryReadFragment(input)) {
		    int msgIdx = PipeReader.getMsgIdx(input);
		    switch(msgIdx) {
		        case MQTTClientResponseSchema.MSG_MESSAGE_3:
			        	
		        	int i = internalTopic.length;
		        	if (allTopicsMatch) {
		        		i = 0;//to select the common converter for all.
		        		
						PipeWriter.presumeWriteFragment(output, MSG_PUBLISH_103);
			        	//direct copy of topic
			        	DataOutputBlobWriter<IngressMessages> stream = PipeWriter.outputStream(output);
			        	DataOutputBlobWriter.openField(stream);
			        	PipeReader.readUTF8(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_TOPIC_23, stream);
			        	DataOutputBlobWriter.closeHighLevelField(stream, MSG_PUBLISH_103_FIELD_TOPIC_1);

		        	} else {
			        	boolean topicMatches = false;
			        	while (--i >= 0) { //TODO: this is very bad, swap out with trie parser instead of linear search
			        		if (PipeReader.isEqual(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_TOPIC_23, externalTopic[i])) {
			        			topicMatches = true;
			        			break;
			        		}
			        	}
			        	assert(topicMatches) : "ERROR, this topic was not known "+PipeReader.readUTF8(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_TOPIC_23, new StringBuilder());
		        		if (!topicMatches) {
		        			logger.warn("Unknown topic from external broker {}",PipeReader.readUTF8(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_TOPIC_23, new StringBuilder()));
		        			break;
		        		}
						PipeWriter.presumeWriteFragment(output, MSG_PUBLISH_103);
						PipeWriter.writeUTF8(output, MSG_PUBLISH_103_FIELD_TOPIC_1, internalTopic[i]);
        	
		        	}		       	
	
					//////////////////
					//copy and convert the data
					//////////////////

					//debug
					//StringBuilder b = new StringBuilder("IngressMQTTStage ");
					//System.err.println(PipeReader.readUTF8(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_PAYLOAD_25 , b));
										
					DataInputBlobReader<MQTTClientResponseSchema> inputStream = PipeReader.inputStream(input, MQTTClientResponseSchema.MSG_MESSAGE_3_FIELD_PAYLOAD_25);
					DataOutputBlobWriter<IngressMessages> outputStream = PipeWriter.outputStream(output);
					DataOutputBlobWriter.openField(outputStream);
					
					converter[i].convertData(inputStream, outputStream);
															
					int length = DataOutputBlobWriter.closeHighLevelField(outputStream, MSG_PUBLISH_103_FIELD_PAYLOAD_3);

					/////////////////////
					/////////////////////
					/////////////////////
					
					PipeWriter.publishWrites(output);					
		            
		        break;

				case MQTTClientResponseSchema.MSG_CONNECTIONATTEMPT_5:
					int connectResponse = PipeReader.readInt(input, MQTTClientResponseSchema.MSG_CONNECTIONATTEMPT_5_FIELD_RESULTCODE_51);
					int sessionPresent = PipeReader.readInt(input, MQTTClientResponseSchema.MSG_CONNECTIONATTEMPT_5_FIELD_SESSIONPRESENT_52);
					connectResult.status = MQTTConnectionStatus.fromSpecification(connectResponse);
					connectResult.sessionPresent = sessionPresent != 0;
					logger.info(connectResult.toString());
					publishConnectionFeedback();
				break;

		        case MQTTClientResponseSchema.MSG_SUBSCRIPTIONRESULT_4:
					int qosSpecification = PipeReader.readInt(input,MQTTClientResponseSchema.MSG_SUBSCRIPTIONRESULT_4_FIELD_MAXQOS_41);
					// TODO: send feedback to business logic
				break;

		        case -1:
		           requestShutdown();
		        break;
		    }
		    PipeReader.releaseReadLock(input);
		}
	}
	private void publishConnectionFeedback() {
		if (connectionFeedbackTopic != null) {
			PipeWriter.presumeWriteFragment(output, IngressMessages.MSG_PUBLISH_103);
			PipeWriter.writeUTF8(output, IngressMessages.MSG_PUBLISH_103_FIELD_TOPIC_1, connectionFeedbackTopic);
			DataOutputBlobWriter<IngressMessages> stream = PipeWriter.outputStream(output);
			DataOutputBlobWriter.openField(stream);
			stream.write(connectResult);
			stream.closeHighLevelField(IngressMessages.MSG_PUBLISH_103_FIELD_PAYLOAD_3);
			PipeWriter.publishWrites(output);
		}
	}
}
