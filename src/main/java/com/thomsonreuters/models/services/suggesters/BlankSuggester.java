package com.thomsonreuters.models.services.suggesters;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;

public class BlankSuggester extends SuggesterHelper implements
		DictionaryLoader<Lookup> {

	@Override
	public Blockable<String, Lookup> getSuggesterList() {
		return new BlockingHashTable<String, Lookup>();
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
