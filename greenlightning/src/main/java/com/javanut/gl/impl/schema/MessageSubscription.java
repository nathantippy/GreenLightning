package com.javanut.gl.impl.schema;

import java.nio.ByteBuffer;

import com.javanut.pronghorn.pipe.*;
public class MessageSubscription extends MessageSchema<MessageSubscription> {


	public final static FieldReferenceOffsetManager FROM = new FieldReferenceOffsetManager(
		    new int[]{0xc0400003,0xa8000000,0xb8000001,0xc0200003,0xc0400003,0x80000000,0x80000001,0xc0200003},
		    (short)0,
		    new String[]{"Publish","Topic","Payload",null,"StateChanged","OldOrdinal","NewOrdinal",null},
		    new long[]{103, 1, 3, 0, 71, 8, 9, 0},
		    new String[]{"global",null,null,null,"global",null,null,null},
		    "MessageSubscriber.xml",
		    new long[]{2, 2, 0},
		    new int[]{2, 2, 0});
	
	public static final MessageSubscription instance = new MessageSubscription();
	
	private MessageSubscription() {
		super(FROM);
	}
    
	public static final int MSG_PUBLISH_103 = 0x00000000;
	public static final int MSG_PUBLISH_103_FIELD_TOPIC_1 = 0x01400001;
	public static final int MSG_PUBLISH_103_FIELD_PAYLOAD_3 = 0x01c00003;
	public static final int MSG_STATECHANGED_71 = 0x00000004;
	public static final int MSG_STATECHANGED_71_FIELD_OLDORDINAL_8 = 0x00000001;
	public static final int MSG_STATECHANGED_71_FIELD_NEWORDINAL_9 = 0x00000002;

	/**
	 *
	 * @param input Pipe arg used for consumePublish and consumeStateChanged
	 */
	public static void consume(Pipe<MessageSubscription> input) {
	    while (PipeReader.tryReadFragment(input)) {
	        int msgIdx = PipeReader.getMsgIdx(input);
	        switch(msgIdx) {
	            case MSG_PUBLISH_103:
	                consumePublish(input);
	            break;
	            case MSG_STATECHANGED_71:
	                consumeStateChanged(input);
	            break;
	            case -1:
	               //requestShutdown();
	            break;
	        }
	        PipeReader.releaseReadLock(input);
	    }
	}

	/**
	 *
	 * @param input Pipe arg used for PipeReader.readUTF8 and PipeReader.readBytes
	 */
	public static void consumePublish(Pipe<MessageSubscription> input) {
	    StringBuilder fieldTopic = PipeReader.readUTF8(input,MSG_PUBLISH_103_FIELD_TOPIC_1,new StringBuilder(PipeReader.readBytesLength(input,MSG_PUBLISH_103_FIELD_TOPIC_1)));
	    ByteBuffer fieldPayload = PipeReader.readBytes(input,MSG_PUBLISH_103_FIELD_PAYLOAD_3,ByteBuffer.allocate(PipeReader.readBytesLength(input,MSG_PUBLISH_103_FIELD_PAYLOAD_3)));
	}

	/**
	 *
	 * @param input Pipe arg used for PipeReader.readInt
	 */
	public static void consumeStateChanged(Pipe<MessageSubscription> input) {
	    int fieldOldOrdinal = PipeReader.readInt(input,MSG_STATECHANGED_71_FIELD_OLDORDINAL_8);
	    int fieldNewOrdinal = PipeReader.readInt(input,MSG_STATECHANGED_71_FIELD_NEWORDINAL_9);
	}

	/**
	 *
	 * @param output Pipe arg used for PipeWriter.presumeWriteFragment
	 * @param fieldTopic CharSequence arg used for PipeWriter.writeUTF8
	 * @param fieldPayloadBacking byte[] arg used for PipeWriter.writeBytes
	 * @param fieldPayloadPosition int arg used for PipeWriter.Bytes
	 * @param fieldPayloadLength int arg used for PipeWriter.Bytes
	 */
	public static void publishPublish(Pipe<MessageSubscription> output, CharSequence fieldTopic, byte[] fieldPayloadBacking, int fieldPayloadPosition, int fieldPayloadLength) {
	        PipeWriter.presumeWriteFragment(output, MSG_PUBLISH_103);
	        PipeWriter.writeUTF8(output,MSG_PUBLISH_103_FIELD_TOPIC_1, fieldTopic);
	        PipeWriter.writeBytes(output,MSG_PUBLISH_103_FIELD_PAYLOAD_3, fieldPayloadBacking, fieldPayloadPosition, fieldPayloadLength);
	        PipeWriter.publishWrites(output);
	}

	/**
	 *
	 * @param output Pipe arg used in PipeWriter.presumeWriteFragment, .writeInt and .publishWrites
	 * @param fieldOldOrdinal int arg used for PipeWriter.writeInt
	 * @param fieldNewOrdinal int arg used for PipeWriter.writeInt
	 */
	public static void publishStateChanged(Pipe<MessageSubscription> output, int fieldOldOrdinal, int fieldNewOrdinal) {
	        PipeWriter.presumeWriteFragment(output, MSG_STATECHANGED_71);
	        PipeWriter.writeInt(output,MSG_STATECHANGED_71_FIELD_OLDORDINAL_8, fieldOldOrdinal);
	        PipeWriter.writeInt(output,MSG_STATECHANGED_71_FIELD_NEWORDINAL_9, fieldNewOrdinal);
	        PipeWriter.publishWrites(output);
	}

        
}
