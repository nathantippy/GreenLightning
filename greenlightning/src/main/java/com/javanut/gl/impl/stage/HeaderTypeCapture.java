package com.javanut.gl.impl.stage;

import com.javanut.gl.api.Headable;
import com.javanut.pronghorn.network.config.HTTPContentType;
import com.javanut.pronghorn.network.config.HTTPContentTypeDefaults;
import com.javanut.pronghorn.network.config.HTTPHeader;
import com.javanut.pronghorn.network.config.HTTPSpecification;
import com.javanut.pronghorn.pipe.ChannelReader;

public class HeaderTypeCapture implements Headable {

	private HTTPContentType type;
	private HTTPSpecification<?, ?, ?, ?> httpSpec;
	
	public HeaderTypeCapture(HTTPSpecification<?, ?, ?, ?> httpSpec) {
		this.httpSpec = httpSpec;
	}
	
	@Override
	public void read(HTTPHeader header, ChannelReader reader) {
		
		short type = reader.available()>=2 ? reader.readShort() : -1;
		if ((type<0) || (type>=httpSpec.contentTypes.length)) {
			this.type = HTTPContentTypeDefaults.UNKNOWN;
		} else {
			this.type = (HTTPContentType)httpSpec.contentTypes[type];
		}
		
	}

	/**
	 *
	 * @return type
	 */
	public HTTPContentType type() {
		return type;
	}

	public void read(HTTPHeader value, ChannelReader reader, long fieldId) {
		read(value,reader.structured().read(fieldId));		
	}

}
