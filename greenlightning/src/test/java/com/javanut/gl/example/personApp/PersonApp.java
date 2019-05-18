package com.javanut.gl.example.personApp;

import com.javanut.gl.api.GreenApp;
import com.javanut.gl.api.GreenFramework;
import com.javanut.gl.api.GreenRuntime;
import com.javanut.pronghorn.struct.StructBuilder;
import com.javanut.pronghorn.struct.StructType;

public class PersonApp implements GreenApp {

	@Override
	public void declareConfiguration(GreenFramework builder) {

		builder.defineRoute().parseJSON()
								.stringField("firstName",GreenField.firstName)
								.stringField("lastName",GreenField.lastName)
								.booleanField("enabled",GreenField.enabled)
								.integerField("age",GreenField.age)
								.integerField("id",GreenField.id)		
				.path("/people").routeId(GreenStruct.person);
		
		builder.defineRoute().path("/people/#{id}").routeId(GreenStruct.getPerson);
		
		builder.defineRoute()
		       .path("/people/#{id}/${char}isable")
		       .path("/people/#{id}/${char}nable")
		       .associatedObject("id", GreenField.id)
		       .associatedObject("char", GreenField.enabled)
		       .routeId(GreenStruct.operatePerson);
		
		builder.defineRoute()
		       .path("/people/${char}isabled")
		       .path("/people/${char}nabled")
		       .associatedObject("char", GreenField.enabled)
		       .routeId(GreenStruct.queryPersons);		
		
		StructBuilder netBase = builder.defineStruct()
		       .addField(GreenField.connectionId, StructType.Long)
		       .addField(GreenField.sequenceId, StructType.Long);
		
		/////
		/////
		
		builder.extendStruct(netBase)
				.addField(GreenField.id, StructType.Long)
				.register(GreenStructInternal.fetchPerson);
		
		builder.extendStruct(netBase)
				.addField(GreenField.id, StructType.Long)
				.addField(GreenField.enabled, StructType.Boolean)
				.register(GreenStructInternal.modifyPersonState);
		
		builder.extendStruct(netBase)
				.addField(GreenField.enabled, StructType.Boolean)
				.register(GreenStructInternal.queryPersonsList);
		
		builder.extendStruct(netBase)
				.addField("firstName",StructType.Text,GreenField.firstName)
				.addField("lastName",StructType.Text,GreenField.lastName)
				.addField("enabled",StructType.Boolean,GreenField.enabled)
				.addField("age",StructType.Long,GreenField.age)
				.addField("id",StructType.Long,GreenField.id)
				.register(GreenStructInternal.adminPersons);       
		
		////
		////
		
		builder.extendStruct(netBase)
		.addField(GreenField.status, StructType.Integer)
		.register(GreenStructResponse.getResponse);
		
		builder.extendStruct(netBase)
		.addField(GreenField.status, StructType.Integer)
		.addField(GreenField.payload, StructType.Text)		
		.register(GreenStructResponse.postResponse);
		
		builder.extendStruct(netBase)
		.addField(GreenField.status, StructType.Integer)
		.addField(GreenField.payload, StructType.Text)		
		.register(GreenStructResponse.chunkedPostResponse);
		
		builder.useHTTP1xServer(8080)
		       .setHost("*.*.*.*");
				
		builder.useSerialStores(1, 1<<17, "passphrase");
		
	}

	@Override
	public void declareBehavior(GreenRuntime runtime) {
		
		runtime.addRestListener(GreenStruct.person.name(), new PersonAdmin(runtime))
		       .includeRoutesByAssoc(GreenStruct.person); //query all, or create one with post
		
		runtime.addRestListener(GreenStruct.getPerson.name(), new PersonQuery(runtime))
	       .includeRoutesByAssoc(GreenStruct.getPerson); //get one by id
	
		runtime.addRestListener(GreenStruct.operatePerson.name(), new PersonUpdate(runtime))
	       .includeRoutesByAssoc(GreenStruct.operatePerson); //modify the state of one by id
	
		runtime.addRestListener(GreenStruct.queryPersons.name(), new PersonListQuery(runtime))
	       .includeRoutesByAssoc(GreenStruct.queryPersons); //get list by state
	
		PersonManager personManager = new PersonManager(runtime);
		runtime.registerListener("PersonManager", personManager)
		   .addSubscription(GreenStructInternal.fetchPerson.name(),personManager::fetch)
		   .addSubscription(GreenStructInternal.modifyPersonState.name(),personManager::modify)
		   .addSubscription(GreenStructInternal.queryPersonsList.name(),personManager::query)
		   .addSubscription(GreenStructInternal.adminPersons.name()+"add",personManager::addPerson)
		   .addSubscription(GreenStructInternal.adminPersons.name()+"dump",personManager::showAll);
		
		ChunkPostResponder chunkResponder = new ChunkPostResponder(runtime);
		runtime.registerListener(GreenStructResponse.chunkedPostResponse.name(),chunkResponder)
			.addSubscription(GreenStructResponse.chunkedPostResponse.name()+"begin", chunkResponder::beginChunks)
		    .addSubscription(GreenStructResponse.chunkedPostResponse.name()+"continued", chunkResponder::continueChunks);
		
		runtime.addPubSubListener(GreenStructResponse.postResponse.name(), new PostResponder(runtime))
		   .addSubscription(GreenStructResponse.postResponse.name());
		
		runtime.addPubSubListener(GreenStructResponse.getResponse.name(), new GetResponder(runtime))
		   .addSubscription(GreenStructResponse.getResponse.name());
	}

}
