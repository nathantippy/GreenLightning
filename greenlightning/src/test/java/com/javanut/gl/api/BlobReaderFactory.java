package com.javanut.gl.api;

import static org.junit.Assert.assertEquals;

import com.javanut.pronghorn.pipe.*;

interface MyConsumer<T> {
    void accept(T t);
}

public class BlobReaderFactory {

    public static ChannelReader generateExtractionDataToTest(MyConsumer<DataOutputBlobWriter<?>> appender) {
        Pipe<RawDataSchema> p = RawDataSchema.instance.newPipe(10, 300);
        p.initBuffers();
        int size = Pipe.addMsgIdx(p, 0);
        DataOutputBlobWriter<?> stream = Pipe.outputStream(p);
        stream.openField();

        ///Here is the data
        appender.accept(stream);

        //Done with the data
        int lenWritten = stream.length();

        stream.closeLowLevelField();
        Pipe.confirmLowLevelWrite(p,size);
        Pipe.publishWrites(p);

        Pipe.takeMsgIdx(p);
        DataInputBlobReader<?> streamOut = Pipe.inputStream(p);
        streamOut.openLowLevelAPIField();

        assertEquals(lenWritten, streamOut.available());

        return streamOut;
    }
}
