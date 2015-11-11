package com.thomsonreuters.models.extact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SuggestDataExt {

	@JsonProperty("source")
	String source;

	@JsonProperty("took")
	String took;

	@JsonProperty("suggestions")
	List<Map<String, String>> suggestions = new ArrayList<Map<String, String>>();

	public String getSource() {
		return source;
	}

	public List<Map<String, String>> getSuggestions() {
		return suggestions;
	}

	public String getTook() {
		return took;
	}

	public SuggestDataExt(@JsonProperty("source") String source,
			@JsonProperty("took") String took,
			@JsonProperty("suggestions") List<Map<String, String>> suggestions) {

		this.source = source;
		this.suggestions = suggestions;
		this.took = took;
	}

}
