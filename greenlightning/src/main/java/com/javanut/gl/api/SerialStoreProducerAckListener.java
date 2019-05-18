package com.javanut.gl.api;

public interface SerialStoreProducerAckListener {

	boolean producerAck(int storeId, long value);

}
