package com.thomsonreuters.models.extact;

import java.util.List;

public interface SuggesterHandlerExt {

	List<SuggestDataExt> lookup(String query, int n);

	/** added **/
	public List<SuggestDataExt> lookup(String path, String query, int n);

	public List<SuggestDataExt> lookup(String query, List<String> sources,
			List<String> infos, int size);

}
