package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuggestData {

	@JsonProperty("source")
	public String source;
	
	@JsonProperty("took")
	public String took;

	@JsonProperty("suggestions")
	public List<Suggestions> suggestions=new ArrayList<SuggestData.Suggestions>();

	public class Suggestions {
		@JsonProperty("keyword")
		public String keyword;

		@JsonProperty("info")
		public List<Info> info = new ArrayList<SuggestData.Info>();

	}

	public class Info {
		@JsonProperty("key")
		public String key;
		@JsonProperty("value")
		public String value;

	}

}