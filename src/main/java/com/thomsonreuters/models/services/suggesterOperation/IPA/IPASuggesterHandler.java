package com.thomsonreuters.models.services.suggesterOperation.IPA;

import java.util.List;

public interface IPASuggesterHandler {
 

	/** added **/
	public String lookup(String path, String query, int n,boolean countchild,boolean showall);
 
}
