package com.javanut.gl.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.FailablePayloadReading;
import com.javanut.gl.api.Payloadable;
import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.pipe.DataOutputBlobWriter;
import com.javanut.pronghorn.pipe.MessageSchema;
import com.javanut.pronghorn.pipe.Pipe;

public class PayloadReader<S extends MessageSchema<S>> extends DataInputBlobReader<S> {

	private static final int INDEX_BASE_OFFSET_FOR_PAYLOAD_POSITION = 1;

	private static final Logger logger = LoggerFactory.getLogger(PayloadReader.class);
	
	public PayloadReader(Pipe<S> pipe) {
        super(pipe);
    }
	
    protected static <S extends MessageSchema<S>> void checkLimit(PayloadReader<S> that, int min) {
      	
    	if ( ((that.position-that.bytesLowBound) + min) > that.length) { 
    		throw new RuntimeException(
    				"Read attempted beyond the end of the field data. Pos:"
    		              +(that.position-that.bytesLowBound)
    		              +" adding:"+min+" must be < "+that.length
    		              +"   ex: "+that.position+" "+that.bytesLowBound
    		              );
    	
    	}
    }

	private int fieldIdx(long fieldId) {
		return INDEX_BASE_OFFSET_FOR_PAYLOAD_POSITION+((int)fieldId & 0xFFFF);
	}

	protected int fieldType(long fieldId) {
		return (((int)fieldId)>>16) & 0xFF;
	}

	public int computePosition(long fieldId) {
		assert(fieldId>=0) : "check field name, it does not match any found field";
		//jump to end and index backwards to find data position
		//logger.info("compute from the end {} value found {} ",fieldIdx(fieldId), readFromEndLastInt(fieldIdx(fieldId)));
		return readFromEndLastInt(fieldIdx(fieldId));	

	}
	
	protected int computePositionSecond(long fieldId) {
		assert(fieldId>=0) : "check field name, it does not match any found field";
		//jump to end and index backwards to find data position
		return readFromEndLastInt(1+fieldIdx(fieldId));		
	}


	/////////////////////

	@Override
	public int read(byte[] b) {
		//not checked because this read will only read available
		return super.read(b);
	}


	@Override
	public int read(byte[] b, int off, int len) {
		//not checked because this read will only read available
		return super.read(b, off, len);
	}


	@Override
	public void readFully(byte[] b) {
		//not checked because this read will only read available
		super.readFully(b);
	}


	@Override
	public void readFully(byte[] b, int off, int len) {
		//not checked because this read will only read available
		super.readFully(b, off, len);
	}


	@Override
	public int skipBytes(int n) {
		//not checked because this read will only read available
		return super.skipBytes(n);
	}


	@Override
	public boolean readBoolean() {
		checkLimit(this,1);
		return super.readBoolean();
	}


	@Override
	public byte readByte() {
		checkLimit(this,1);
		return super.readByte();
	}


	@Override
	public int readUnsignedByte() {
		checkLimit(this,1);
		return super.readUnsignedByte();
	}


	@Override
	public short readShort() {
		checkLimit(this,2);
		return super.readShort();
	}


	@Override
	public int readUnsignedShort() {
		checkLimit(this,2);
		return super.readUnsignedShort();
	}


	@Override
	public char readChar() {
		checkLimit(this,1);
		return super.readChar();
	}


	@Override
	public int readInt() {
		checkLimit(this,4);
		return super.readInt();
	}


	@Override
	public long readLong() {
		checkLimit(this,8);
		return super.readLong();
	}


	@Override
	public float readFloat() {
		checkLimit(this,4);
		return super.readFloat();
	}


	@Override
	public double readDouble() {
		checkLimit(this,8);
		return super.readDouble();
	}


	@Override
	public int read() {
		//returns -1 if we have no data so no need to check.
		return super.read();
	}


	@Override
	public String readLine() {
		checkLimit(this,1);
		return super.readLine();
	}


	@Override
	public String readUTF() {
		checkLimit(this,2);
		return super.readUTF();
	}


	@Override
	public <A extends Appendable> A readUTF(A target) {
		checkLimit(this,2);
		return super.readUTF(target);
	}


	@Override
	public Object readObject() {
        //bounds are already checked here
		return super.readObject();
	}


	@Override
	public <T extends MessageSchema<T>> void readInto(DataOutputBlobWriter<T> writer, int length) {
		checkLimit(this,length);
		super.readInto(writer, length);
	}


	@Override
	public <A extends Appendable> A readPackedChars(A target) {
		checkLimit(this,1);
		return super.readPackedChars(target);
	}


	@Override
	public long readPackedLong() {
		checkLimit(this,1);
		return super.readPackedLong();
	}


	@Override
	public int readPackedInt() {
		checkLimit(this,1);
		return super.readPackedInt();
	}


	@Override
	public double readDecimalAsDouble() {
		checkLimit(this,2);
		return super.readDecimalAsDouble();
	}


	@Override
	public long readDecimalAsLong() {
		checkLimit(this,2);
		return super.readDecimalAsLong();
	}


	@Override
	public short readPackedShort() {
		checkLimit(this,1);
		return super.readPackedShort();
	}

	/**
	 *
	 * @param reader Payloadable arg used to read(this)
	 * @return if (hasRemainingBytes()) true else false
	 */
	public boolean openPayloadData(Payloadable reader) {
		if (hasRemainingBytes()) {			
			reader.read(this.structured().readPayload());//even when we have zero length...
			return true;
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param reader FailablePayloadReading arg used to read(this)
	 * @return if (hasRemainingBytes()) return reader.read(this) else false
	 */
	public boolean openPayloadDataFailable(FailablePayloadReading reader) {
		if (hasRemainingBytes()) {
			return reader.read(this.structured().readPayload());//even when we have zero length...
		} else {
			return false;
		}
	}

}
