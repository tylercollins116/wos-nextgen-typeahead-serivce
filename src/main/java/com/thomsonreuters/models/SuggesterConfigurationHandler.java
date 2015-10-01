package com.thomsonreuters.models;

import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;

public interface SuggesterConfigurationHandler {
	
	public DictionaryLoader<AnalyzingSuggester> getDictionaryAnalyzer();

}
