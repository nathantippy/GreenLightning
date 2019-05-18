package com.javanut.gl.api;

import com.javanut.json.JSONAccumRule;
import com.javanut.json.JSONAligned;
import com.javanut.json.JSONRequired;
import com.javanut.json.decode.JSONExtractor;
import com.javanut.json.decode.JSONTable;
import com.javanut.pronghorn.struct.ByteSequenceValidator;
import com.javanut.pronghorn.struct.DecimalValidator;
import com.javanut.pronghorn.struct.LongValidator;

final class ExtractedJSONFieldsForClientImpl implements ExtractedJSONFieldsForClient {

	private final ClientHostPortConfig clientHostPortConfig;
	private long timeoutNS;

	/**
	 * @param clientHostPortConfig
	 */
	ExtractedJSONFieldsForClientImpl(ClientHostPortConfig clientHostPortConfig, long timeoutNS) {
		this.clientHostPortConfig = clientHostPortConfig;
		this.timeoutNS = timeoutNS;
	}

	JSONTable<JSONExtractor> ex = new JSONExtractor().begin();

	public ExtractedJSONFieldsForClient setTimeoutNS(long timeoutNS) {
		this.timeoutNS = timeoutNS;
		return this;
	}
	
	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient stringField(JSONAligned isAligned, JSONAccumRule accumRule,
																String extractionPath, T field) {
		Object temp = ex.stringField(isAligned, accumRule, extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient stringField(String extractionPath, T field) {
		Object temp = ex.stringField(extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient integerField(JSONAligned isAligned, JSONAccumRule accumRule,
			String extractionPath, T field) {
		Object temp = ex.integerField(isAligned, accumRule,extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient integerField(String extractionPath, T field) {
		Object temp = ex.integerField(extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient decimalField(JSONAligned isAligned, JSONAccumRule accumRule,
			String extractionPath, T field) {
		Object temp = ex.decimalField(isAligned, accumRule, extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient decimalField(String extractionPath, T field) {
		Object temp = ex.decimalField(extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient booleanField(JSONAligned isAligned, JSONAccumRule accumRule,
			String extractionPath, T field) {
		Object temp = ex.booleanField(isAligned, accumRule, extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient booleanField(String extractionPath, T field) {
		Object temp = ex.booleanField(extractionPath, field);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public ClientHostPortInstance finish() {
		return new ClientHostPortInstance(this.clientHostPortConfig.host, this.clientHostPortConfig.port, ex.finish(), timeoutNS);
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient integerField(String extractionPath, T field,
			JSONRequired required, LongValidator validator) {
		Object temp = ex.integerField(extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient stringField(String extractionPath, T field,
			JSONRequired required, ByteSequenceValidator validator) {
		Object temp = ex.stringField(extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient decimalField(String extractionPath, T field,
			JSONRequired required, DecimalValidator validator) {
		Object temp = ex.decimalField(extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient integerField(JSONAligned isAligned,
			JSONAccumRule accumRule, String extractionPath, T field, JSONRequired required, LongValidator validator) {
		Object temp = ex.integerField(isAligned, accumRule,extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient stringField(JSONAligned isAligned,
			JSONAccumRule accumRule, String extractionPath, T field, JSONRequired required, ByteSequenceValidator validator) {
		Object temp = ex.stringField(isAligned, accumRule,extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient decimalField(JSONAligned isAligned,
			JSONAccumRule accumRule, String extractionPath, T field, JSONRequired required, DecimalValidator validator) {
		Object temp = ex.decimalField(isAligned, accumRule,extractionPath, field, required, validator);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient booleanField(String extractionPath, T field,
			JSONRequired isRequired) {
		Object temp = ex.booleanField(extractionPath, field, isRequired);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}

	@Override
	public <T extends Enum<T>> ExtractedJSONFieldsForClient booleanField(JSONAligned isAligned, JSONAccumRule accumRule,
			String extractionPath, T field, JSONRequired isRequired) {
		Object temp = ex.booleanField(isAligned, accumRule, extractionPath, field, isRequired);
		assert(temp == ex) : "internal error, the same instance should have been returned";
		return this;
	}
}