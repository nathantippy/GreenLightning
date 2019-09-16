package com.javanut.gl.api;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javanut.gl.impl.BridgeConfigImpl;
import com.javanut.gl.impl.BuilderImpl;
import com.javanut.gl.impl.ChildClassScanner;
import com.javanut.gl.impl.ChildClassScannerVisitor;
import com.javanut.gl.impl.GreenFrameworkImpl;
import com.javanut.gl.impl.MessageReader;
import com.javanut.gl.impl.blocking.BalanceBlockingBehavior;
import com.javanut.gl.impl.blocking.JoinBlockingBehavior;
import com.javanut.gl.impl.blocking.TargetSelector;
import com.javanut.gl.impl.schema.MessageSubscription;
import com.javanut.gl.impl.schema.TrafficOrderSchema;
import com.javanut.gl.impl.stage.EgressConverter;
import com.javanut.gl.impl.stage.IngressConverter;
import com.javanut.gl.impl.stage.PendingStageBuildable;
import com.javanut.gl.impl.stage.ReactiveListenerStage;
import com.javanut.gl.impl.stage.ReactiveManagerPipeConsumer;
import com.javanut.pronghorn.network.HTTPServerConfig;
import com.javanut.pronghorn.network.HTTPServerConfigImpl;
import com.javanut.pronghorn.network.NetGraphBuilder;
import com.javanut.pronghorn.network.ServerCoordinator;
import com.javanut.pronghorn.network.ServerNewConnectionStage;
import com.javanut.pronghorn.network.ServerPipesConfig;
import com.javanut.pronghorn.network.http.HTTPRouterStageConfig;
import com.javanut.pronghorn.network.module.FileReadModuleStage;
import com.javanut.pronghorn.network.module.ResourceModuleStage;
import com.javanut.pronghorn.network.schema.HTTPLogRequestSchema;
import com.javanut.pronghorn.network.schema.HTTPLogResponseSchema;
import com.javanut.pronghorn.network.schema.HTTPRequestSchema;
import com.javanut.pronghorn.network.schema.NetPayloadSchema;
import com.javanut.pronghorn.network.schema.ReleaseSchema;
import com.javanut.pronghorn.network.schema.ServerResponseSchema;
import com.javanut.pronghorn.pipe.DataInputBlobReader;
import com.javanut.pronghorn.pipe.Pipe;
import com.javanut.pronghorn.pipe.PipeConfig;
import com.javanut.pronghorn.pipe.PipeConfigManager;
import com.javanut.pronghorn.pipe.RawDataSchema;
import com.javanut.pronghorn.pipe.util.hash.IntHashTable;
import com.javanut.pronghorn.stage.PronghornStage;
import com.javanut.pronghorn.stage.blocking.BlockingWorkStage;
import com.javanut.pronghorn.stage.blocking.BlockingWorkerProducer;
import com.javanut.pronghorn.stage.route.ReplicatorStage;
import com.javanut.pronghorn.stage.scheduling.GraphManager;
import com.javanut.pronghorn.stage.scheduling.StageScheduler;
import com.javanut.pronghorn.stage.test.PipeCleanerStage;

public class MsgRuntime<B extends BuilderImpl, L extends ListenerFilter, G extends MsgRuntime<B,L,G> > {
 
    public static final Logger logger = LoggerFactory.getLogger(MsgRuntime.class);


	private ServerCoordinator serverCoord;
	
    
    protected static final int nsPerMS = 1_000_000;
    public B builder;

	protected final GraphManager gm;

    protected final String[] args;

     
    protected String telemetryHost;
    
    protected void setScheduler(StageScheduler scheduler) {
    	builder.setScheduler(scheduler);
    	
    	//now that we have the scheduler we know that the graph is fully complete.
    	//this our only opportunity to inject any private topic names.    	
    	populatePrivateTopicPipeNames();    	
    }

    public static BuilderImpl builder(MsgRuntime that) {
    	return that.builder;
    }
    

	private void populatePrivateTopicPipeNames() {
		try {
			Field f = builder.gm.getClass().getDeclaredField("pipeDOTSchemaNames");
			f.setAccessible(true);
			byte[][] names = (byte[][])f.get(builder.gm);
			builder.populatePrivateTopicPipeNames(names);
		} catch (NoSuchFieldException e) {
			logger.warn("unable to set names for private topic pipes.",e);
		} catch (SecurityException e) {
			logger.warn("unable to set names for private topic pipes.",e);
		} catch (IllegalArgumentException e) {
			logger.warn("unable to set names for private topic pipes.",e);
		} catch (IllegalAccessException e) {
			logger.warn("unable to set names for private topic pipes.",e);
		}
	}
        
	public StageScheduler getScheduler() {
	      return builder.getScheduler();
	}    
    
    //NOTE: keep short since the MessagePubSubStage will STOP consuming message until the one put on here
    //      is actually taken off and consumed.  We have little benefit to making this longer.
    protected static final int defaultCommandChannelSubscriberLength = 8;
    
    public static final int defaultCommandChannelLength = 32;
    public static final int defaultCommandChannelMaxPayload = 64;

    protected boolean transducerAutowiring = true;

	private PipeConfig<HTTPRequestSchema> fileRequestConfig;

    protected int netResponsePipeIdx = -1;

    
    private BridgeConfig[] bridges = new BridgeConfig[0];
    
	protected int parallelInstanceUnderActiveConstruction = -1;
	
	protected Pipe<?>[] outputPipes = null;
    protected ChildClassScannerVisitor listenerAndNameVisitor = new ChildClassScannerVisitor<MsgCommandChannel>() {
    	
		@Override
		public boolean visit(MsgCommandChannel cmdChnl, Object topParent, String topName) {
			    
            IntHashTable usageChecker = getUsageChecker();
            if (null!=usageChecker) {
				if (!ChildClassScanner.notPreviouslyHeld(cmdChnl, topParent, usageChecker)) {
	            	logger.error("Command channel found in "+
	            			topParent.getClass().getSimpleName()+
	            	             " can not be used in more than one Behavior");                	
	            	assert(false) : "A CommandChannel instance can only be used exclusivly by one object or lambda. Double check where CommandChannels are passed in.";                   
	            }
            }
            
            MsgCommandChannel.setListener(cmdChnl, (Behavior)topParent, topName);

			return true;//keep looking
					
		}

    };
    
    public void disableTransducerAutowiring() {
    	transducerAutowiring = false;
    }
    
    private void keepBridge(BridgeConfig bridge) {
    	boolean isFound = false;
    	int i = bridges.length;
    	while (--i>=0) {
    		isFound |= bridge == bridges[0];
    	}
    	if (!isFound) {
    		
    		i = bridges.length;
    		BridgeConfig[] newArray = new BridgeConfig[i+1];
    		System.arraycopy(bridges, 0, newArray, 0, i);
    		newArray[i] = bridge;
    		bridges = newArray;
    		
    	}
    }
    
	public MsgRuntime(String[] args, String name) {
		 this.gm = new GraphManager(name);
		 this.args = args != null ? args : new String[0];
	}

    public String[] args() {
    	return args;
    }
    
	public final <T,S> S bridgeSubscription(CharSequence topic, BridgeConfig<T,S> config) {		
		long id = ((BridgeConfigImpl<T,S>) config).addSubscription(topic);
		keepBridge(config);
		return config.subscriptionConfigurator(id);
	}
	public final <T,S> S bridgeSubscription(CharSequence internalTopic, CharSequence extrnalTopic, BridgeConfig<T,S> config) {		
		long id = ((BridgeConfigImpl<T,S>) config).addSubscription(internalTopic,extrnalTopic);
		keepBridge(config);
		return config.subscriptionConfigurator(id);
	}
	public final <T,S> S bridgeSubscription(CharSequence internalTopic, CharSequence extrnalTopic, BridgeConfig<T,S> config, IngressConverter converter) {		
		long id = ((BridgeConfigImpl<T,S>) config).addSubscription(internalTopic,extrnalTopic,converter);
		keepBridge(config);
		return config.subscriptionConfigurator(id);
	}
	
	public final <T,S> T bridgeTransmission(CharSequence topic, BridgeConfig<T,S> config) {		
		long id = ((BridgeConfigImpl<T,S>) config).addTransmission(topic);
		keepBridge(config);
		return config.transmissionConfigurator(id);
	}
	public final <T,S> T bridgeTransmission(CharSequence internalTopic, CharSequence extrnalTopic, BridgeConfig<T,S> config) {		
		long id = ((BridgeConfigImpl<T,S>) config).addTransmission(internalTopic, extrnalTopic);
		keepBridge(config);
		return config.transmissionConfigurator(id);
	}	
	public final <T,S> T bridgeTransmission(CharSequence internalTopic, CharSequence extrnalTopic, BridgeConfig<T,S> config, EgressConverter converter) {		
		long id = ((BridgeConfigImpl<T,S>) config).addTransmission(internalTopic, extrnalTopic,converter);
		keepBridge(config);
		return config.transmissionConfigurator(id);
	}	
	
    public final L addRestListener(RestListener listener) {
    	return (L) registerListenerImpl(listener);
    }
    
    public final L addFileWatchListener(FileWatchListener listener) {
    	return (L) registerListenerImpl(listener);
    }
        
    public final L addResponseListener(HTTPResponseListener listener) {
    	return (L) registerListenerImpl(listener);
    }
    
    public final L addStartupListener(StartupListener listener) {
        return (L) registerListenerImpl(listener);
    }
	    
    public final L addShutdownListener(ShutdownListener listener) {
        return (L) registerListenerImpl(listener);
    }	
    
    public final L addTimePulseListener(TimeListener listener) {
        return (L) registerListenerImpl(listener);
    }
    
    public final L addPubSubListener(PubSubListener listener) {
        return (L) registerListenerImpl(listener);
    }

    public final <E extends Enum<E>> L addStateChangeListener(StateChangeListener<E> listener) {
        return (L) registerListenerImpl(listener);
    }
      
    public L registerListener(Behavior listener) {
    	return (L) registerListenerImpl(listener);
    }
    
/////    
    
    public final L addRestListener(String id, RestListener listener) {
    	return (L) registerListenerImpl(id, listener);
    }
      
    public final L addFileWatchListener(String id, FileWatchListener listener) {
    	return (L) registerListenerImpl(id, listener);
    }
    
    public final L addResponseListener(String id, HTTPResponseListener listener) {
    	return (L) registerListenerImpl(id, listener);
    }
    
    public final L addStartupListener(String id, StartupListener listener) {
        return (L) registerListenerImpl(id, listener);
    }
	    
    public final L addShutdownListener(String id, ShutdownListener listener) {
        return (L) registerListenerImpl(id, listener);
    }	
    
    public final L addTimePulseListener(String id, TimeListener listener) {
        return (L) registerListenerImpl(id, listener);
    }
    
    public final L addPubSubListener(String id, PubSubListener listener) {
        return (L) registerListenerImpl(id, listener);
    }

    public final <E extends Enum<E>> L addStateChangeListener(String id, StateChangeListener<E> listener) {
        return (L) registerListenerImpl(id, listener);
    }
      
    public L registerListener(String id, Behavior listener) {
    	return (L) registerListenerImpl(id, listener);
    }
    
    protected void logStageScheduleRates() {
        int totalStages = GraphManager.countStages(gm);
           for(int i=1;i<=totalStages;i++) {
               PronghornStage s = GraphManager.getStage(gm, i);
               if (null != s) {
                   
                   Object rate = GraphManager.getNota(gm, i, GraphManager.SCHEDULE_RATE, null);
                   if (null == rate) {
                       logger.debug("{} is running without breaks",s);
                   } else  {
                       logger.debug("{} is running at rate of {}",s,rate);
                   }
               }
               
           }
    }
    
    public String getArgumentValue(String longName, String shortName, String defaultValue) {
    	return getOptArg(longName,shortName, args, defaultValue);
    }
    
    public boolean hasArgument(String longName, String shortName) {
    	return hasArg(longName, shortName, this.args);
    }

    public static String getOptArg(String longName, String shortName, String[] args, String defaultValue) {
        
        String prev = null;
        for (String token : args) {
            if (longName.equals(prev) || shortName.equals(prev)) {
                if (token == null || token.trim().length() == 0 || token.startsWith("-")) {
                    return defaultValue;
                }
                return reportChoice(longName, shortName, token.trim());
            }
            prev = token;
        }
        return reportChoice(longName, shortName, defaultValue);
    }
    

    public static boolean hasArg(String longName, String shortName, String[] args) {
        for(String token : args) {
            if(longName.equals(token) || shortName.equals(token)) {
            	reportChoice(longName, shortName, "");
                return true;
            }
        }
        return false;
    }
    
    static String reportChoice(final String longName, final String shortName, final String value) {
        System.out.append(longName).append(" ").append(shortName).append(" ").append(value).append("\n");
        return value;
    }
    


    public void shutdownRuntime() {
    	shutdownRuntime(3);
    }
    
    public void shutdownRuntime(final int secondsTimeout) {
    
    	
    	//only do if not already done.
    	if (!isShutdownRequested()) {
    		logger.info("shutdownRuntime({}) with timeout",secondsTimeout);
    	
	    	if ((null == builder) || (null == builder.getScheduler()) ) {
	    		//logger.warn("No runtime activity was detected.");
	    		//System.exit(0); //not sure we want this because we call this from another app..
	    		return;
	    	}
	    
	    	builder.requestShutdown(secondsTimeout);

    	}
    }

	public boolean isShutdownRequested() {
		return ReactiveListenerStage.isShutdownRequested(builder);
	}
    
	public boolean isShutdownComplete() {
		return ReactiveListenerStage.isShutdownComplete(builder);
	}

	//////////
    //only build this when assertions are on
    //////////
    public static IntHashTable cmdChannelUsageChecker;
    static {
        assert(setupForChannelAssertCheck());
    }    
    private static boolean setupForChannelAssertCheck() {
        cmdChannelUsageChecker = new IntHashTable(9);
        return true;
    }    
    private static IntHashTable getUsageChecker() {
    	return cmdChannelUsageChecker;
    }

	public static Pipe<MessageSubscription> buildMessageSubscriptionPipe(BuilderImpl b) {
		
	    Pipe<MessageSubscription> subscriptionPipe = new Pipe<MessageSubscription>(b.pcm.getConfig(MessageSubscription.class)) {
			@SuppressWarnings("unchecked")
			@Override
			protected DataInputBlobReader<MessageSubscription> createNewBlobReader() {
				return new MessageReader(this);
			}
		};
		return subscriptionPipe;
	}
	
	protected void constructingParallelInstance(int i) {
		parallelInstanceUnderActiveConstruction = i;
	}
	
	public int constructingParallelInstance() {
		return parallelInstanceUnderActiveConstruction;
	}
	
	public int totalParallelInstances() {
		return builder.parallelTracks();
	}
	
	protected void constructingParallelInstancesEnding() {
		parallelInstanceUnderActiveConstruction = -1;
	}
	
	//////////////////
	//server and other behavior
	//////////////////
	@SuppressWarnings("unchecked")
	public void declareBehavior(MsgApp app) {
		logger.trace("begin declare behavior");
		//The server and telemetry http hosts/ports MUST be defined before we begin 
		//the declaration of behaviors because we must do binding to the host names.
		//as a result this finalize must happen early.
		builder.finalizeDeclareConnections();
		////////////////////////////////////////////
		
		logger.trace("finalize declare connections");
		
		if (builder.getHTTPServerConfig() != null) {
		    buildGraphForServer(app);
		} else {            	
			app.declareBehavior(this);
			
			if (null != builder.behaviorDefinition()) {
				int parallelism = builder.parallelTracks();
				//since server was not started and did not create each parallel instance this will need to be done here
	   
				for(int i = 0;i<parallelism;i++) { //do not use this loop, we will loop inside server setup..
			    	constructingParallelInstance(i);
			    	builder.behaviorDefinition().declareBehavior((GreenRuntime)this);                
			    }
			}
			//////////////
			//create real stages now for each of the behaviors
			//////////////
			constructingBridges();
			builder.initAllPendingReactors();
			
		}
		
		logger.trace("begin contruct parallel endings");
		
		constructingParallelInstancesEnding();
		
		
	}

	private void constructingBridges() {
		//Init bridges		
		int b = bridges.length;
		while (--b>=0) {
			((BridgeConfigImpl)bridges[b]).finalizeDeclareConnections(this);
		}
	}
	
	private void buildGraphForServer(MsgApp app) {

		HTTPServerConfig config = builder.getHTTPServerConfig();

		logger.trace("begin buildGraphForServer");
		
		//////////////////////////
		//////////////////////////
		
		((HTTPServerConfigImpl) config).setTracks(builder.parallelTracks());
		HTTPServerConfigImpl r = ((HTTPServerConfigImpl) config);
						
		r.pcmOut.ensureSize(ServerResponseSchema.class, 4, r.getMaxResponseSize());	
		
		r.pcmIn.ensureSize(ReleaseSchema.class, 1<<16, 0);//for high volume
		r.pcmOut.ensureSize(ReleaseSchema.class, 1<<16, 0);//for high volume

		ServerPipesConfig serverConfig = new ServerPipesConfig(
					r.logFileConfig(),
					r.isTLS(),
					r.getMaxConnectionBits(),
					r.tracks(),
					r.getEncryptionUnitsPerTrack(),
					r.getConcurrentChannelsPerEncryptUnit(),
					r.getDecryptionUnitsPerTrack(),
					r.getConcurrentChannelsPerDecryptUnit(),				
					//one message might be broken into this many parts
					r.getSocketToParserBlocks(), 
					r.getMaxRequestSize(),
					r.getMaxResponseSize(),
					config.getMaxQueueIn(),
					config.getMaxQueueOut(),
					r.pcmIn,r.pcmOut);

		
		int groups = builder.getGroupsCount();
		
		serverCoord = new ServerCoordinator(
				config.getCertificates(),
				config.bindHost(), 
				config.bindPort(),
				config.connectionStruct(),
				false,
				"Server",
				config.defaultHostPath(),
				serverConfig);
	
		
		//NOTE  serverConfig.maxConcurrentInputs = serverRequestUnwrapUnits * concurrentChannelsPerDecryptUnit
		//this is only 600MB for this NetPayloadSchema inputs of 280   building: 280 of Primary:15 Secondary:21 NetPayloadSchema total: 623902720
		//System.out.println("building: "+serverConfig.maxConcurrentInputs+" of "+serverConfig.incomingDataConfig+" total: "+(serverConfig.maxConcurrentInputs*serverConfig.incomingDataConfig.totalBytesAllocated()));
		
		
		final Pipe<NetPayloadSchema>[] encryptedIncomingGroup = Pipe.buildPipes(serverConfig.maxConcurrentInputs, 
											serverConfig.pcmIn.getConfig(NetPayloadSchema.class));           
		
		
		Pipe[] acks = NetGraphBuilder.buildSocketReaderStage(
				           gm, serverCoord,
						   builder.getGroupsCount(), builder.getTracksPerGroup(), 
						   encryptedIncomingGroup);
		               
		
		Pipe[] handshakeIncomingGroup=null;
		Pipe[] planIncomingGroup;
		
		if (config.isTLS()) {
			planIncomingGroup = Pipe.buildPipes(serverConfig.maxConcurrentInputs,
												serverConfig.pcmIn.getConfig(NetPayloadSchema.class));  
			handshakeIncomingGroup = NetGraphBuilder.populateGraphWithUnWrapStages(gm, serverCoord, 
					                      serverConfig.serverRequestUnwrapUnits, 
					                    	serverConfig.pcmIn.getConfig(NetPayloadSchema.class),
					                      encryptedIncomingGroup, planIncomingGroup, acks);
		} else {
			planIncomingGroup = encryptedIncomingGroup;
		}
		////////////////////////
		////////////////////////

		//Must call here so the beginning stages of the graph are drawn first when exporting graph.
		app.declareBehavior(this);
		buildModulesForServer(app);
		
		//////////////
		//create real stages now for each of the behaviors
		//////////////
		constructingBridges();
		builder.initAllPendingReactors();
		

		buildLastHalfOfGraphForServerImpl(serverConfig, serverCoord, builder.parallelTracks(), acks, handshakeIncomingGroup,
				planIncomingGroup);
	}

//	////////////////
//	//future feature
//	//TODO: needs to proctect against mutliple servers configured at once
//	//TODO: needs to add support to keep each parallel group separate.
//	public void declareBehavior(int tracks, DeclareBehavior db) {
//		//TODO: assert this is never called in old parallel code.
//		for (int i = 0; i < tracks; i++) {
//			//TODO: setup the instancee
//			//TODO: what about single server??
//			
//			db.declareBehavior(i);
//		}
//	}
	
	
	private void buildModulesForServer(MsgApp app) {
		////////////////////////
		//create the working modules
		//////////////////////////

		if (null != builder.behaviorDefinition()) {
			
			int p = builder.parallelTracks();
			
			for (int i = 0; i < p; i++) {
				constructingParallelInstance(i);
				builder.behaviorDefinition().declareBehavior((G)this);  //this creates all the modules for this parallel instance								
			}	
		} else {
			if (builder.parallelTracks()>1) {
				throw new UnsupportedOperationException(
						"Remove call to parallelism("+builder.parallelTracks()+") OR make the application implement GreenAppParallel or something extending it.");
			}
		}
	}

	private void buildLastHalfOfGraphForServerImpl(ServerPipesConfig serverConfig, ServerCoordinator serverCoord,
			final int trackCounts, Pipe[] acks, Pipe[] handshakeIncomingGroup, Pipe[] planIncomingGroup) {
		//////////////////
		//////////////////

		final HTTPRouterStageConfig routerConfig = builder.routerConfig();
				
		
		/////////////////
		/////////////////
		
		ArrayList<Pipe> forPipeCleaner = new ArrayList<Pipe>();
		Pipe<HTTPRequestSchema>[][] fromRouterToModules = new Pipe[trackCounts][];	
		int t = trackCounts;
		int totalRequestPipes = 0;
		while (--t>=0) {
			int routeIndex = routerConfig.totalRoutesCount();

			////////////////
			///for catch all
			///////////////
			if (routeIndex==0) {
				routeIndex=1;
			}
			/////////////
			
			fromRouterToModules[t] = new Pipe[routeIndex]; 
		    while (--routeIndex >= 0) {
		    	
		    	ArrayList<Pipe<HTTPRequestSchema>> requestPipes = builder.buildFromRequestArray(t, routeIndex);
		    	
		    	//with a single pipe just pass it one, otherwise use the replicator to fan out from a new single pipe.
		    	final int size = requestPipes.size();
		    	totalRequestPipes += size;
		    	
				if (1 == size) {
		    		fromRouterToModules[t][routeIndex] = requestPipes.get(0);
		    		
		    	} else {
		    		//we only create a pipe when we are about to use the replicator
		    		fromRouterToModules[t][routeIndex] = builder.newHTTPRequestPipe(builder.pcm.getConfig(HTTPRequestSchema.class));		    		
		    		if (0 == size) {
		    			logger.info("warning there are routes without any consumers");
		    			//we have no consumer so tie it to pipe cleaner		    		
		    			forPipeCleaner.add(fromRouterToModules[t][routeIndex]);
		    		} else {
		    			assert(requestPipes.size() == size);
		    			ReplicatorStage.newInstance(gm, fromRouterToModules[t][routeIndex], requestPipes.toArray(new Pipe[requestPipes.size()]));	
		    		}
		    	}
		    }
		    if (0==totalRequestPipes) {
		    	logger.warn("ERROR: includeRoutes or includeAllRoutes must be called on REST listener.");
		    }
		}
		
		if (!forPipeCleaner.isEmpty()) {
			PipeCleanerStage.newInstance(gm, forPipeCleaner.toArray(new Pipe[forPipeCleaner.size()]));
		}
		
		//NOTE: building arrays of pipes grouped by parallel/routers heading out to order supervisor		
		Pipe<ServerResponseSchema>[][] fromModulesToOrderSuper = new Pipe[trackCounts][];
		Pipe<ServerResponseSchema>[] errorResponsePipes = new Pipe[trackCounts];
		PipeConfig<ServerResponseSchema> errConfig = ServerResponseSchema.instance.newPipeConfig(4, 512);
		
		final boolean catchAll = routerConfig.totalPathsCount()==0;
				
		
		int spaceForEchos = serverCoord.connectionStruct().inFlightPayloadSize();
		
		int trackId = trackCounts;
		while (--trackId>=0) {
			Pipe<ServerResponseSchema>[] temp = fromModulesToOrderSuper[trackId] = builder.buildToOrderArray(trackId);			
			//this block is required to make sure the ordering stage has room
			int c = temp.length;
			while (--c>=0) {
				//ensure that the ordering stage can consume messages of this size
				serverConfig.ensureServerCanWrite((spaceForEchos+temp[c].config().maxVarLenSize()));
			}		
		}
		serverConfig.ensureServerCanWrite((spaceForEchos+errConfig.maxVarLenSize()));
		//TODO: use ServerCoordinator to hold information about log?
		Pipe<HTTPLogRequestSchema>[] reqLog = new Pipe[trackCounts];
		Pipe<HTTPLogResponseSchema>[] resLog = new Pipe[trackCounts];
		Pipe[][] perTrackFromNet = Pipe.splitPipes(trackCounts, planIncomingGroup);

		NetGraphBuilder.buildLogging(gm, serverCoord, reqLog, resLog);
		
		NetGraphBuilder.buildRouters(gm, serverCoord, acks,
				fromModulesToOrderSuper, fromRouterToModules, routerConfig, catchAll, reqLog, perTrackFromNet);

		Pipe<NetPayloadSchema>[] fromOrderedContent = NetGraphBuilder.buildRemainderOfServerStagesWrite(gm, serverCoord, handshakeIncomingGroup);
		
		//////////////////////
		/////////////////////
		ServerNewConnectionStage newConStage = new ServerNewConnectionStage(gm, serverCoord); 
		serverCoord.processNota(gm, newConStage);
		////////////////////
		///////////////////
		
		
		//NOTE: the fromOrderedContent must hold var len data which is greater than fromModulesToOrderSuper
		assert(fromOrderedContent.length >= trackCounts) : "reduce track count since we only have "+fromOrderedContent.length+" pipes";
		
		Pipe<NetPayloadSchema>[][] perTrackFromSuper = Pipe.splitPipes(trackCounts, fromOrderedContent);
				
				
		NetGraphBuilder.buildOrderingSupers(gm, serverCoord, fromModulesToOrderSuper, resLog, perTrackFromSuper);
	}
	
	
	public void setExclusiveTopics(MsgCommandChannel cc, String ... exlusiveTopics) {
		// TODO Auto-generated method stub
		
		throw new UnsupportedOperationException("Not yet implemented");
		
	}


	//Not for general consumption, only used when we need the low level Pipes to connect directly to the pub/sub or other dynamic subsystem.
	public static GraphManager getGraphManager(MsgRuntime runtime) {
		return runtime.gm;
	}
	
	
	public RouteFilter addFileServer(String path) { //adds server to all routes
		final int parallelIndex = (-1 == parallelInstanceUnderActiveConstruction) ? 0 : parallelInstanceUnderActiveConstruction;
                		
        //due to internal implementation we must keep the same number of outputs as inputs.
		Pipe<HTTPRequestSchema>[] inputs = new Pipe[1];
		Pipe<ServerResponseSchema>[] outputs = new Pipe[1];		
		populateHTTPInOut(inputs, outputs, 0, parallelIndex);
			
		FileReadModuleStage.newInstance(gm, inputs, outputs, builder.httpSpec, buildFilePath(path));
				
		return new StageRouteFilter(inputs[0], builder, parallelIndex);
	}

	public RouteFilter addResourceServer(String resourceRoot) {
		return addResourceServer(resourceRoot,"index.html");
	}
	
	
	public RouteFilter addResourceServer(String resourceRoot, String resourceDefault) {
		final int parallelIndex = (-1 == parallelInstanceUnderActiveConstruction) ? 0 : parallelInstanceUnderActiveConstruction;
		
		//due to internal implementation we must keep the same number of outputs as inputs.
		Pipe<HTTPRequestSchema>[] inputs = new Pipe[1];
		Pipe<ServerResponseSchema>[] outputs = new Pipe[1];		
		populateHTTPInOut(inputs, outputs, 0, parallelIndex);
				
		ResourceModuleStage.newInstance(gm, inputs, outputs, builder.httpSpec, resourceRoot, resourceDefault);
		//The associated routes need the Accept-Encoding header			
		return new StageRouteFilter(inputs[0], builder, parallelIndex);
				
	}

	
	
	
	private void populateHTTPInOut(Pipe<HTTPRequestSchema>[] inputs, 
			                      Pipe<ServerResponseSchema>[] outputs, 
			                      int idx, int parallelIndex) {
		
		if (null == fileRequestConfig) {
			fileRequestConfig = builder.pcm.getConfig(HTTPRequestSchema.class).grow2x();
		}
		
		inputs[idx] = builder.newHTTPRequestPipe(fileRequestConfig);

		outputs[idx] = builder.newNetResponsePipe(this.serverCoord.pcmOut.getConfig(ServerResponseSchema.class), parallelIndex);

	}
	

    //TODO: needs a lot of re-work.
	private File buildFilePath(String path) {
		// Strip URL space tokens from incoming path strings to avoid issues.
        // TODO: This should be made garbage free.
        // TODO: Is this expected behavior?
        path = path.replaceAll("\\Q%20\\E", " ");

        //TODO: MUST FIND PATH...
        Enumeration<URL> resource;
		try {
			resource = ClassLoader.getSystemResources(path);
		
			while (resource.hasMoreElements()) {
				System.err.println("looking for resoruce: "+path+" and found "+String.valueOf(resource.nextElement()));
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        		
        	//	ClassLoader.getSystemResource(path);
        
        
        File rootPath = new File(path);
        
        if (!rootPath.exists()) {
        	//test if this is under development
        	File devPath = new File("./src/main/resources/"+path);
        	if (devPath.exists()) {
        		rootPath = devPath;
        	}
        }
       
        
        if (!rootPath.exists()) {
        	throw new UnsupportedOperationException("Path not found: "+rootPath);
        }
		return rootPath;
	}

    ///////////////////////////
	//end of file server
	///////////////////////////
		
	public <P extends BlockingWorkerProducer & TargetSelector> L registerBlockingListener(
			P producer,	Object chooserFieldAssoc, String topicToBlockingTask, String topicFromBlockingTask) {
		int threadsCount = 64;//default
		return registerBlockingListener(producer, chooserFieldAssoc, threadsCount, topicToBlockingTask, topicFromBlockingTask);
	}

	public <P extends BlockingWorkerProducer & TargetSelector> L registerBlockingListener(
			P producer,
			Object chooserFieldAssoc,
			int threadsCount,
			String topicToBlockingTask, String topicFromBlockingTask) {
		return registerBlockingListener(null, producer, threadsCount, chooserFieldAssoc, topicToBlockingTask, topicFromBlockingTask);
	}
	

	public <P extends BlockingWorkerProducer & TargetSelector> L registerBlockingListener(String behaviorName, P producer,
			final int threadsCount, Object chooserFieldAssoc, String topicToBlockingTask, String ... topicsFromBlockingTask) {
		
		if (null == behaviorName) {
			//by default unless a name is given use the behavior
			behaviorName = topicToBlockingTask+"->"+(topicsFromBlockingTask.length==1?topicsFromBlockingTask[0]:Arrays.toString(topicsFromBlockingTask).replaceAll(" ",""));
		}
		
		final String name = behaviorName;
		final int capturedTrack = parallelInstanceUnderActiveConstruction;
		
		behaviorName = builder.validateUniqueName(behaviorName, parallelInstanceUnderActiveConstruction);	
		
		//byte[] track = parallelInstanceUnderActiveConstruction<0 ? null : BuilderImpl.trackNameBuilder(parallelInstanceUnderActiveConstruction);
				
		final MsgRuntime<B, L, G> r = this;
		
		final PipeConfig commonConfig = RawDataSchema.instance.newPipeConfig(2*1024, 500*14); //TODO: too small??
		final Pipe<RawDataSchema>[] toBlockingWork = Pipe.buildPipes(threadsCount, commonConfig);
		final Pipe<RawDataSchema>[] fromBlockingWork = Pipe.buildPipes(threadsCount, commonConfig);
		
		
		ListenerFilter balanceWork = r.registerListener("BalanceWork", new BalanceBlockingBehavior(toBlockingWork, chooserFieldAssoc))
				                               .addSubscription(topicToBlockingTask);
		((ReactiveListenerStage)balanceWork).addOutputPronghornPipes(toBlockingWork);
		
		
		JoinBlockingBehavior join = new JoinBlockingBehavior(r, 
															 fromBlockingWork,								 
											                 topicsFromBlockingTask, 
											                 producer);		
		
		
		ListenerFilter joinWork = r.registerListener("JoinWork", join);
		((ReactiveListenerStage)joinWork).addInputPronghornPipes(fromBlockingWork);
		

		PendingStageBuildable pendingBuilder = new PendingStageBuildable() {

			@Override
			public void initRealStage() {			
					new BlockingWorkStage(gm, toBlockingWork, fromBlockingWork, producer);
				}
	
			@Override
			public String behaviorName() {
				 return name;
				}
		};

		/////////////////////////////////////remove?
		builder.pendingInit(pendingBuilder);//not needed if we use normal register listener

		//if this producer is also a behavior register it.
		if (producer instanceof Behavior) {
			return registerListener((Behavior)producer);
		}		
		
		return null;
	}
	

	///////////////////////////
	
	
    public Builder getBuilder(){
    	if(this.builder==null){    	    
    	    this.builder = (B) new GreenFrameworkImpl(gm,args);
    	}
    	return this.builder;
    }
        
    private ListenerFilter registerListenerImpl(final Behavior listener) {
    	return registerListenerImpl(null, listener);
    }
    
    private ListenerFilter registerListenerImpl(String id, final Behavior listener) {
    	
    	if (null == id) {
    		//by default unless a name is given use the behavior
    		id = builder.generateBehaviorName(listener);
    	}
    	
    	////////////
    	//OUTPUT
    	///////////
    	outputPipes = new Pipe<?>[0];
    	
    	//assigns the listener and id to any CommandChannels so they know where they belong for private topics later...
    	ChildClassScanner.visitUsedByClass(id, listener, listenerAndNameVisitor, MsgCommandChannel.class);//populates outputPipes

        
        //this is empty when transducerAutowiring is off
        final ArrayList<ReactiveManagerPipeConsumer> consumers = new ArrayList<ReactiveManagerPipeConsumer>(); 
        
        //extract this into common method to be called in GL and FL
        Pipe<?>[] inputPipes = new Pipe<?>[0];
		if (transducerAutowiring) {
			inputPipes = autoWireTransducers(id, listener, inputPipes, consumers);
		}
		
        return builder.createReactiveListener(gm, listener, 
        		                                inputPipes, outputPipes, consumers,
        		                                parallelInstanceUnderActiveConstruction,id);
        
    }

	protected Pipe<?>[] autoWireTransducers(final String topName, final Behavior listener, Pipe<?>[] inputPipes,
			final ArrayList<ReactiveManagerPipeConsumer> consumers) {
		
		if (inputPipes.length==0) {
			return inputPipes;//no work since no inputs are used.
		}
		
		final Grouper g = new Grouper(inputPipes);
		
		ChildClassScannerVisitor tVisitor = new ChildClassScannerVisitor() {
			@Override
			public boolean visit(Object child, Object topParent, String topName) {					
				if (g.additions()==0) {
					//add first value
					Pipe[] pipes = builder.operators.createPipes(builder, listener, g);
					consumers.add(new ReactiveManagerPipeConsumer(listener, builder.operators, pipes));
					g.add(pipes);
				}					
				
				int c = consumers.size();
				while (--c>=0) {
					if (consumers.get(c).behavior == child) {
						//do not add this one it is already recorded
						return true;
					}
				}
				
				Pipe[] pipes = builder.operators.createPipes(builder, child, g);
				consumers.add(new ReactiveManagerPipeConsumer(child, builder.operators, pipes));
				g.add(pipes);		
				
				return true;
			}				
		};
		
		ChildClassScanner.visitUsedByClass(topName, listener, tVisitor, ListenerTransducer.class);
					
		if (g.additions()>0) {
			inputPipes = g.firstArray();
			g.buildReplicators(gm, consumers);
		}
		return inputPipes;
	}

	protected PipeConfigManager buildPipeManager() {
		PipeConfigManager pcm = new PipeConfigManager(4,2,128);
		pcm.addConfig(defaultCommandChannelLength,0,TrafficOrderSchema.class );
		return pcm;
	}

	public static IntHashTable getSubPipeLookup(MsgRuntime runtime) {
		return runtime.builder.getSubPipeLookup();
	}

	
	public void addCleanShutdownRunnable(Runnable cleanRunnable) {
		builder.setCleanShutdownRunnable(cleanRunnable);
	}

	public void addDirtyShutdownRunnable(Runnable dirtyRunnable) {
		builder.setDirtyShutdownRunnable(dirtyRunnable);
	}

    
}
