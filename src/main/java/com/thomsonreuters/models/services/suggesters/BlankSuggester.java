package com.thomsonreuters.models.services.suggesters;

import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;

public class BlankSuggester extends SuggesterHelper implements
		DictionaryLoader<AnalyzingSuggester> {

	@Override
	public Blockable<String, AnalyzingSuggester> getSuggesterList() {
		return new BlockingHashTable<String, AnalyzingSuggester>();
	}

	@Override
	public void reloadDictionary(String propertyName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeSuggesterList() {
		// TODO Auto-generated method stub

	}

}
