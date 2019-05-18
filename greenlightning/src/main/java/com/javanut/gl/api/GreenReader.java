package com.javanut.gl.api;

import com.javanut.pronghorn.pipe.ChannelReader;
import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.util.TrieParser;
import com.javanut.pronghorn.util.TrieParserReader;

public class GreenReader extends GreenExtractor {

	private final static boolean alwaysCompletePayloads = true;
	private final TrieParser tp;
	
	GreenReader(TrieParser tp) {		
		super(new TrieParserReader(alwaysCompletePayloads));
		this.tp = tp;
	}

	@SuppressWarnings("unchecked")
	public void beginRead(ChannelReader reader) {
		TrieParserReader.parseSetup(tpr, (DataInputBlobReader)reader);
	}
	
	@SuppressWarnings("unchecked")
	public void beginRead(ChannelReader reader, int maxBytes) {
		TrieParserReader.parseSetup(tpr, (DataInputBlobReader)reader, maxBytes);
	}
	
	public long readToken() {
		return TrieParserReader.parseNext(tpr, tp);
	}
		
	public int skipByte() {
		return TrieParserReader.parseSkipOne(tpr);
	}
	
	public boolean hasMore() {		
		return TrieParserReader.parseHasContent(tpr);
	}
	
	
	public static GreenReader examplePrepare() {
		
		return new GreenTokenMap()
				    .add(1234, "type: %b\n")
				    .add(3322, "age: %i\n")
	                .add(1,    " ") //white space
				    .newReader();
		
	}
	
	public static void exampleConsume(GreenReader reader, ChannelReader blob) {
		
		reader.beginRead(blob);
		while (reader.hasMore()) {
			
			long token = reader.readToken();
			
			switch ((int)token) {
				case 1234: //this is a token id
					
					//may call methods here to capture extractions
					
					//copyExtractedUTF8ToAppendable(idx, target)
				    //extractedLong(idx)
				    
					break;
				default:
					//unknown
					reader.skipByte();
			}
			
			
			
		}
		
		
		
	}
	
}
