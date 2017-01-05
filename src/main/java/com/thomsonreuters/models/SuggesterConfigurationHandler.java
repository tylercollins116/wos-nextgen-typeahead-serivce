package com.thomsonreuters.models;

import java.util.Set;

import org.apache.lucene.search.suggest.Lookup;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;

public interface SuggesterConfigurationHandler {

	public DictionaryLoader<Lookup> getDictionaryAnalyzer();
	public ElasticEntityProperties getElasticEntityProperties(String esPath);
	public Set<String> getRegisteredElasticEntityNames();
}
