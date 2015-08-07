package com.thomsonreuters.models.services.suggesterOperation;

import java.io.IOException;

import com.thomsonreuters.models.services.util.Blockable;

public interface DictionaryLoader<K> {

	public Blockable<String, K> getSuggesterList();
	
	public void reloadDictionary(String propertyName) throws IOException;
	
	public void initializeSuggesterList() throws IOException;

}
