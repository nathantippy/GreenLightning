package com.javanut.gl.api;

import com.javanut.json.JSONAccumRule;
import com.javanut.json.JSONAligned;
import com.javanut.json.JSONRequired;
import com.javanut.pronghorn.struct.ByteSequenceValidator;
import com.javanut.pronghorn.struct.DecimalValidator;
import com.javanut.pronghorn.struct.LongValidator;

public interface ExtractedJSONFields<E extends ExtractedJSONFields<?>> {
	
    public <T extends Enum<T>> E integerField(String extractionPath, T field);    
    public <T extends Enum<T>> E stringField(String extractionPath, T field);    
    public <T extends Enum<T>> E decimalField(String extractionPath, T field);    
    public <T extends Enum<T>> E booleanField(String extractionPath, T field);   
    
    public <T extends Enum<T>> E integerField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field);
    public <T extends Enum<T>> E stringField(JSONAligned isAligned,  JSONAccumRule accumRule,  String extractionPath, T field);
    public <T extends Enum<T>> E decimalField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field);
    public <T extends Enum<T>> E booleanField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field);
    
    public <T extends Enum<T>> E integerField(String extractionPath, T field, JSONRequired isRequired, LongValidator validator);    
    public <T extends Enum<T>> E stringField(String extractionPath, T field, JSONRequired isRequired, ByteSequenceValidator validator);    
    public <T extends Enum<T>> E decimalField(String extractionPath, T field, JSONRequired isRequired, DecimalValidator validator);    
    public <T extends Enum<T>> E booleanField(String extractionPath, T field, JSONRequired isRequired); 
    
    public <T extends Enum<T>> E integerField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field, JSONRequired isRequired, LongValidator validator);
    public <T extends Enum<T>> E stringField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field, JSONRequired isRequired, ByteSequenceValidator validator);
    public <T extends Enum<T>> E decimalField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field, JSONRequired isRequired, DecimalValidator validator);
    public <T extends Enum<T>> E booleanField(JSONAligned isAligned, JSONAccumRule accumRule, String extractionPath, T field, JSONRequired isRequired);
        
}
