package com.thomsonreuters.models;

import java.util.List;
import java.util.Map;

public interface SuggesterHandler {

	public List<SuggestData> lookup(String query, int n);

	public List<SuggestData> lookup(String path, String query, int n);
	
	
	

	/** added **/
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos, int size,String uid);

	public List<SuggestData> lookup(String query, int size, String uid,
			boolean all);
	
	 
}
