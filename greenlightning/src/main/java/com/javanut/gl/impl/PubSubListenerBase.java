package com.javanut.gl.impl;

import com.javanut.gl.api.MsgRuntime;
import com.javanut.gl.api.PubSubMethodListener;
import com.javanut.pronghorn.pipe.ChannelReader;


public interface PubSubListenerBase extends PubSubMethodListenerBase {

    /**
     * Invoked when a new publication is received from the {@link MsgRuntime}.
     *
     * @param topic Topic of the publication.
     * @param payload {@link ChannelReader} for the topic contents.
     */
    boolean message(CharSequence topic, ChannelReader payload);
}
