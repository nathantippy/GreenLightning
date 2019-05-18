package com.javanut.gl.impl.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.api.ListenerFilter;
import com.javanut.gl.api.MQTTBridge;
import com.javanut.gl.api.MQTTQoS;
import com.javanut.gl.api.MQTTWriter;
import com.javanut.gl.api.MsgRuntime;
import com.javanut.gl.api.Writable;
import com.javanut.gl.impl.BridgeConfigImpl;
import com.javanut.gl.impl.BridgeConfigStage;
import com.javanut.gl.impl.stage.EgressConverter;
import com.javanut.gl.impl.stage.EgressMQTTStage;
import com.javanut.gl.impl.stage.IngressConverter;
import com.javanut.gl.impl.stage.IngressMQTTStage;
import com.javanut.gl.impl.stage.ReactiveListenerStage;
import com.javanut.pronghorn.network.SSLUtil;
import com.javanut.pronghorn.network.TLSCertificates;
import com.javanut.pronghorn.network.TLSCerts;
import com.javanut.pronghorn.network.mqtt.MQTTClientGraphBuilder;
import com.javanut.pronghorn.network.mqtt.MQTTEncoder;
import com.javanut.pronghorn.network.schema.MQTTClientRequestSchema;
import com.javanut.pronghorn.network.schema.MQTTClientResponseSchema;
import com.javanut.pronghorn.pipe.DataOutputBlobWriter;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeConfig;
import com.javanut.pronghorn.pipe.PipeWriter;
import com.javanut.pronghorn.stage.scheduling.GraphManager;
import com.javanut.pronghorn.stage.test.PipeCleanerStage;
import com.javanut.pronghorn.stage.test.PipeNoOp;
import com.javanut.pronghorn.util.ArrayGrow;

public class MQTTConfigImpl extends BridgeConfigImpl<MQTTConfigTransmission,MQTTConfigSubscription> implements MQTTBridge {

	private final static Logger logger = LoggerFactory.getLogger(MQTTConfigImpl.class);
	// MQTT
	public static final int DEFAULT_MAX_MQTT_IN_FLIGHT = 10;
	public static final int DEFAULT_MAX__MQTT_MESSAGE = 1<<12;

	private final CharSequence host;
	private final int port;
	private final CharSequence clientId;
	private int keepAliveSeconds = 10; //default
	//
	private CharSequence user = null;
	private CharSequence pass = null;
	private CharSequence lastWillTopic = null;
	private CharSequence connectionFeedbackTopic;
	private Writable lastWillPayload = null;
	//
	private int flags;
	private TLSCertificates certificates;
	
	private final short maxInFlight;
	private int maximumLenghOfVariableLengthFields;

	private BridgeConfigStage configStage = BridgeConfigStage.Construction;
	
	private Pipe<MQTTClientRequestSchema> clientRequest;
	private Pipe<MQTTClientResponseSchema> clientResponse;
	private final long rate;
	private final GraphManager gm;
	
	private int subscriptionQoS = 0;

	private int transmissionFieldQOS = 0; 
	private int transmissionFieldRetain = 0;
	
	public MQTTConfigImpl(CharSequence host, int port, CharSequence clientId,
			       GraphManager gm, long rate, 
			       short maxInFlight, int maxMessageLength) {
		
		this.host = host;
		this.port = port;
		this.clientId = clientId;
		this.gm = gm;
		this.rate = rate;
		this.maxInFlight = maxInFlight;
		this.maximumLenghOfVariableLengthFields = maxMessageLength;
	}

	public void beginDeclarations() {
		configStage = BridgeConfigStage.DeclareConnections;
	}

    private static Pipe<MQTTClientRequestSchema> newClientRequestPipe(PipeConfig<MQTTClientRequestSchema> config) {
    	return new Pipe<MQTTClientRequestSchema>(config) {
			@SuppressWarnings("unchecked")
			@Override
			protected DataOutputBlobWriter<MQTTClientRequestSchema> createNewBlobWriter() {
				return new MQTTWriter(this);
			}    		
    	};
    }
	
	//send on construction, do not save		
	private void publishBrokerConfig(Pipe<MQTTClientRequestSchema> output) {
		PipeWriter.presumeWriteFragment(output, MQTTClientRequestSchema.MSG_BROKERCONFIG_100);
			
	    PipeWriter.writeUTF8(output,MQTTClientRequestSchema.MSG_BROKERCONFIG_100_FIELD_HOST_26, (CharSequence) host);
	    PipeWriter.writeInt(output,MQTTClientRequestSchema.MSG_BROKERCONFIG_100_FIELD_PORT_27, port);
	    PipeWriter.publishWrites(output);
				
	}
	
	//send upon complete construction
	private void publishConnect(Pipe<MQTTClientRequestSchema> output) {

		PipeWriter.presumeWriteFragment(output, MQTTClientRequestSchema.MSG_CONNECT_1);
			
	    PipeWriter.writeInt(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_KEEPALIVESEC_28, keepAliveSeconds);
	    PipeWriter.writeInt(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_FLAGS_29, flags);
	    PipeWriter.writeUTF8(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_CLIENTID_30, (CharSequence) clientId);
	    PipeWriter.writeUTF8(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_WILLTOPIC_31, (CharSequence) lastWillTopic);
	    
	    DataOutputBlobWriter<MQTTClientRequestSchema> writer = PipeWriter.outputStream(output);
	    DataOutputBlobWriter.openField(writer);	
	    if(null!= lastWillPayload) {
	    	lastWillPayload.write((MQTTWriter)writer);
	    }
	    DataOutputBlobWriter.closeHighLevelField(writer, MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_WILLPAYLOAD_32);
	    
	    PipeWriter.writeUTF8(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_USER_33, (CharSequence) user);
	    PipeWriter.writeUTF8(output,MQTTClientRequestSchema.MSG_CONNECT_1_FIELD_PASS_34, (CharSequence) pass);
	    PipeWriter.publishWrites(output);
	}
	
	public MQTTBridge keepAliveSeconds(int seconds) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);
		keepAliveSeconds = seconds;
		return this;
	}
	
	/**
	 * Clean session ensures the server will not remember state from
	 * previous connections from this client.  In order to ensure QOS 
	 * across restarts of the client this should be set to true.
	 * 
	 * By default this is false.
	 * 
	 * @param clean boolean for clean sessions
	 */
	public MQTTBridge cleanSession(boolean clean) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);
		flags = setBitByBoolean(flags, clean, MQTTEncoder.CONNECT_FLAG_CLEAN_SESSION_1);
		return this;
	}

	private int setBitByBoolean(int target, boolean clean, int bit) {
		if (clean) {
			target = target|bit;
		} else {
			target = (~target)&bit;
		}
		return target;
	}

	/**
	 *
	 * @return useTLS(TLSCertificates.defaultCerts)
	 */
	public MQTTBridge useTLS() {
		return useTLS(TLSCerts.define());
	}

	/**
	 *
	 * @param certificates TLSCertificates arg
	 * @return this
	 */
	public MQTTBridge useTLS(TLSCertificates certificates) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);
		assert(null != certificates);
		this.certificates = certificates;
		this.maximumLenghOfVariableLengthFields = Math.max(this.maximumLenghOfVariableLengthFields, SSLUtil.MinTLSBlock);
		return this;
	}

	/**
	 *
	 * @param user CharSequence arg to authenticate user
	 * @param pass CharSequence arg to authenticate password
	 * @return this.authentication(user, pass, null==this.certificates ? TLSCertificates.defaultCerts: this.certificates)
	 */
	public MQTTBridge authentication(CharSequence user, CharSequence pass) {
		return this.authentication(user, pass, null==this.certificates ? TLSCerts.define(): this.certificates);
	}

	/**
	 *
	 * @param user CharSequence arg to authenticate user
	 * @param pass CharSequence arg to authenticate password
	 * @param certificates TLSCertificates arg used to validate
	 * @return if(user, pass, certificates !== null) return this
	 */
	public MQTTBridge authentication(CharSequence user, CharSequence pass, TLSCertificates certificates) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);
		flags |= MQTTEncoder.CONNECT_FLAG_USERNAME_7;
		flags |= MQTTEncoder.CONNECT_FLAG_PASSWORD_6;

		this.user = user;
		this.pass = pass;

		if (null==user) {
			throw new UnsupportedOperationException("User must not be null");
		}
		if (null==pass) {
			throw new UnsupportedOperationException("Pass must not be null");
		}
		if (null==certificates) {
			throw new UnsupportedOperationException("Certificates must not be null");
		}
		
		return this;
	}

	/**
	 *
	 * @param certificates TLSCertificates arg used to assert that certificates != null
	 * @return this
	 */
	public MQTTBridge authentication(TLSCertificates certificates) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);

		logger.warn("Security Risk: User and Pass should be used when using certificates.");
		
		assert(null != certificates);

		return this;
	}
	
	@Override
	public MQTTBridge subscriptionQoS(MQTTQoS qos) {
		subscriptionQoS = qos.getSpecification();
		return this;
	}

	@Override
	public MQTTBridge transmissionQoS(MQTTQoS qos) {
		transmissionFieldQOS = qos.getSpecification();
		return this;
	}

	@Override
	public MQTTBridge transmissionRetain(boolean value) {		
		transmissionFieldRetain = setBitByBoolean(transmissionFieldRetain, value, MQTTEncoder.CONNECT_FLAG_WILL_RETAIN_5 );
		return this;
	}

	@Override
	public MQTTBridge lastWill(CharSequence topic, boolean retain, MQTTQoS qos, Writable payload) {
		configStage.throwIfNot(BridgeConfigStage.DeclareConnections);
		assert(null!=topic);

		flags |= MQTTEncoder.CONNECT_FLAG_WILL_FLAG_2;
		if (retain) {
			flags |= MQTTEncoder.CONNECT_FLAG_WILL_RETAIN_5;
		}
		byte qosFlag = (byte) (qos.getSpecification() << 3);
		flags |= qosFlag;

		this.lastWillTopic = topic;
		this.lastWillPayload = payload;

		return this;
	}


	public MQTTBridge connectionFeedbackTopic(CharSequence connectFeedbackTopic) {
		this.connectionFeedbackTopic = connectFeedbackTopic;
		return this;
	}

	private void ensureConnected() {
		if (configStage == BridgeConfigStage.DeclareBehavior) {
			return;
		} else {
			//No need for this pipe to be large since we can only get one at a time from the MessagePubSub feeding EngressMQTTStage
			int egressPipeLength = 32;
			
			PipeConfig<MQTTClientRequestSchema> newPipeConfig = MQTTClientRequestSchema.instance.newPipeConfig(egressPipeLength, maximumLenghOfVariableLengthFields);
			
			clientRequest = newClientRequestPipe(newPipeConfig);
			clientRequest.initBuffers();
			
			PipeConfig<MQTTClientResponseSchema> newPipeConfig2 = MQTTClientResponseSchema.instance.newPipeConfig((int) maxInFlight, maximumLenghOfVariableLengthFields);
			
			clientResponse = new Pipe<MQTTClientResponseSchema>(newPipeConfig2);

			final byte totalConnectionsInBits = 2; //only 4 brokers
			final short maxPartialResponses = 1;

			if (null == user || null == pass) {
				logger.warn("no user or pass has been set for this MQTT client connection");
			}
			
			MQTTClientGraphBuilder.buildMQTTClientGraph(gm, certificates,
					                              maxInFlight,
					                              maximumLenghOfVariableLengthFields, 
					                              clientRequest, clientResponse, rate, 
					                              totalConnectionsInBits,
					                              maxPartialResponses,
					                              user,pass);
			
			//send the broker details
			publishBrokerConfig(clientRequest);
			//send the connect msg
			publishConnect(clientRequest);
			configStage = BridgeConfigStage.DeclareBehavior;
		}
	}
	
	private CharSequence[] internalTopicsXmit = new CharSequence[0];
	private CharSequence[] externalTopicsXmit = new CharSequence[0];
	private EgressConverter[] convertersXmit = new EgressConverter[0];
	private int[] qosXmit = new int[0];
	private int[] retainXmit = new int[0];
	
	
	
	private CharSequence[] internalTopicsSub = new CharSequence[0];
	private CharSequence[] externalTopicsSub = new CharSequence[0];
	private IngressConverter[] convertersSub = new IngressConverter[0];
    private int[] qosSub = new int[0];		
	
	@Override
	public long addSubscription(CharSequence internalTopic, CharSequence externalTopic) {
		ensureConnected();
		
		internalTopicsSub = ArrayGrow.appendToArray(internalTopicsSub, internalTopic);
		externalTopicsSub = ArrayGrow.appendToArray(externalTopicsSub, externalTopic);
		convertersSub = ArrayGrow.appendToArray(convertersSub,IngressMQTTStage.copyConverter);
		qosSub = ArrayGrow.appendToArray(qosSub, subscriptionQoS);
		
		assert(internalTopicsSub.length == externalTopicsSub.length);
		assert(internalTopicsSub.length == convertersSub.length);
		assert(internalTopicsSub.length == qosSub.length);
		
		return internalTopicsSub.length-1;
	}
	
	@Override
	public long addSubscription(CharSequence internalTopic, CharSequence externalTopic, IngressConverter converter) {
		ensureConnected();
		
		internalTopicsSub = ArrayGrow.appendToArray(internalTopicsSub, internalTopic);
		externalTopicsSub = ArrayGrow.appendToArray(externalTopicsSub, externalTopic);
		convertersSub = ArrayGrow.appendToArray(convertersSub,converter);
		qosSub = ArrayGrow.appendToArray(qosSub, subscriptionQoS);
		
		assert(internalTopicsSub.length == externalTopicsSub.length);
		assert(internalTopicsSub.length == convertersSub.length);
		assert(internalTopicsSub.length == qosSub.length);
		
		return internalTopicsSub.length-1;
	}


	@Override
	public long addTransmission(CharSequence internalTopic, CharSequence externalTopic) {
		ensureConnected();

		//logger.trace("added subscription to {} in order to transmit out to  ",internalTopic, externalTopic);

		internalTopicsXmit = ArrayGrow.appendToArray(internalTopicsXmit, internalTopic);
		externalTopicsXmit = ArrayGrow.appendToArray(externalTopicsXmit, externalTopic);
		convertersXmit = ArrayGrow.appendToArray(convertersXmit,EgressMQTTStage.copyConverter);
		qosXmit = ArrayGrow.appendToArray(qosXmit, transmissionFieldQOS);
		retainXmit = ArrayGrow.appendToArray(retainXmit, transmissionFieldRetain);		
		
		assert(internalTopicsXmit.length == externalTopicsXmit.length);
		assert(internalTopicsXmit.length == convertersXmit.length);
		assert(internalTopicsXmit.length == qosXmit.length);
		
		return internalTopicsXmit.length-1;
		
	}

	@Override
	public long addTransmission(CharSequence internalTopic, CharSequence externalTopic, EgressConverter converter) {
		ensureConnected();

		internalTopicsXmit = ArrayGrow.appendToArray(internalTopicsXmit, internalTopic);
		externalTopicsXmit = ArrayGrow.appendToArray(externalTopicsXmit, externalTopic);
		convertersXmit = ArrayGrow.appendToArray(convertersXmit,converter);
		qosXmit = ArrayGrow.appendToArray(qosXmit, transmissionFieldQOS);
				
		assert(internalTopicsXmit.length == externalTopicsXmit.length);
		assert(internalTopicsXmit.length == convertersXmit.length);
		assert(internalTopicsXmit.length == qosXmit.length);
		
		return internalTopicsXmit.length-1;
	}

	
	/**
	 *
	 * @param msgRuntime MsgRuntime arg used in EgressMQTTStage
	 */
	public void finalizeDeclareConnections(MsgRuntime<?,?,?> msgRuntime) {
		configStage = BridgeConfigStage.Finalized;
		assert(internalTopicsXmit.length == externalTopicsXmit.length);
		assert(internalTopicsXmit.length == convertersXmit.length);
		
		assert(internalTopicsSub.length == externalTopicsSub.length);
		assert(internalTopicsSub.length == convertersSub.length);
		
		if (internalTopicsSub.length>0) {
			
			//now publish all our subscription requests
			int i = externalTopicsSub.length;
			while (--i>=0) {			
				PipeWriter.presumeWriteFragment(clientRequest, MQTTClientRequestSchema.MSG_SUBSCRIBE_8);
				PipeWriter.writeInt(clientRequest,MQTTClientRequestSchema.MSG_SUBSCRIBE_8_FIELD_QOS_21, qosSub[i]);
				PipeWriter.writeUTF8(clientRequest,MQTTClientRequestSchema.MSG_SUBSCRIBE_8_FIELD_TOPIC_23, externalTopicsSub[i]);
				PipeWriter.publishWrites(clientRequest);
			}

			ListenerFilter registerListener = msgRuntime.registerListener("IngressMQTT",
							new IngressMQTTBehavior(msgRuntime, externalTopicsSub, internalTopicsSub, convertersSub, 
													connectionFeedbackTopic, clientResponse)
						);	
				
			((ReactiveListenerStage)registerListener).addInputPronghornPipes(clientResponse);
			
		} else {
			PipeCleanerStage.newInstance(gm, clientResponse);
		}
		
		if (internalTopicsXmit.length>0) {

			ListenerFilter registerListener = msgRuntime.registerListener("EgressMQTT", new EgressMQTTBehavior(
									internalTopicsXmit, 
			                        externalTopicsXmit, 
			                        qosXmit, retainXmit, 
			                        convertersXmit, clientRequest						
					));
			
			for(int i = 0; i<internalTopicsXmit.length; i++) {
				registerListener.addSubscription(internalTopicsXmit[i]);
			}
			
			((ReactiveListenerStage)registerListener).addOutputPronghornPipes(clientRequest);

		} else {
			PipeNoOp.newInstance(gm, clientRequest);			
		}
	}

	private int activeRow = -1;	
	private final MQTTConfigTransmission transConf = new MQTTConfigTransmission() {
		@Override
		public MQTTConfigTransmission setQoS(MQTTQoS qos) {
			qosXmit[activeRow] = qos.getSpecification();
			return transConf;
		}

		@Override
		public MQTTConfigTransmission setRetain(boolean retain) {
			retainXmit[activeRow] = retain?1:0;
			return transConf;
		}
	};
	private final MQTTConfigSubscription subsConf = new MQTTConfigSubscription() {
		@Override
		public void setQoS(MQTTQoS qos) {
			qosSub[activeRow] = qos.getSpecification();
		}
	};

	@Override
	public MQTTConfigTransmission transmissionConfigurator(long id) {
		activeRow = (int)id;
		return transConf;
	}

	@Override
	public MQTTConfigSubscription subscriptionConfigurator(long id) {
		activeRow = (int)id;
		return subsConf;
	}
}
