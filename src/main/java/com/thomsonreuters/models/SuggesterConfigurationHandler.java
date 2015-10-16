package com.thomsonreuters.models;

import org.apache.lucene.search.suggest.Lookup;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;

public interface SuggesterConfigurationHandler {

	public DictionaryLoader<Lookup> getDictionaryAnalyzer();

}
