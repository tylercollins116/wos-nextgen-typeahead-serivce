package com.thomsonreuters.models;

import java.util.List;

public interface SuggesterHandler {

	public List<SuggestData> lookup(String query, int n);

	public List<SuggestData> lookup(String path, String query, int n, boolean highLight);
	
	
	

	/** added **/
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos, int size,String uid, boolean highLight);

	public List<SuggestData> lookup(String query, int size, String uid,
			boolean all, boolean highLight);
	
	public List<SuggestData> lookup(String query, String source, int offset, 
			int size, String uid, boolean highLight);

//	// Tyler defined function
//	public List<SuggestData> lookup(String query);

	// Tyler defined function
	public List<String> lookup(String query);
}
