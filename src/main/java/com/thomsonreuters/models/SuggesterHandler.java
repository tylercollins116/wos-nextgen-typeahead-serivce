package com.thomsonreuters.models;

import java.util.List;

public interface SuggesterHandler {

	public List<SuggestData> lookup(String query, int n);

	public List<SuggestData> lookup(String path, String query, int n);
	
		/** added **/
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos);

}
