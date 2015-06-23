package com.thomsonteuters.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuggestData {
	String value;
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SuggestData(){
		
	}
	
	public SuggestData(@JsonProperty("value")String value) {
		this.value = value;
	}
	
}