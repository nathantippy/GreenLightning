package com.javanut.gl.impl.file;

import com.javanut.gl.impl.PayloadReader;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.RawDataSchema;

public class FilePayloadReader extends PayloadReader<RawDataSchema> {
	
	public FilePayloadReader(Pipe<RawDataSchema> pipe) {
		super(pipe);
	}
	
	
	//TODO:can position and read extracted parts of file name
	//TODO:can position and read extracted internal content.
	
}
