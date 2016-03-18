package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Before;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.Property;

public class SuggesterMainTest {

	Suggester suggester = null;

	@Before
	public void beforeMainTest() {

		DictionaryLoader<Lookup> dictionaryLoader = new DictionaryLoader<Lookup>() {

			protected final Blockable<String, Lookup> suggesterList = new BlockingHashTable<String, Lookup>();

			@Override
			public Blockable<String, Lookup> getSuggesterList() {
				// TODO Auto-generated method stub
				return suggesterList;
			}

			@Override
			public void reloadDictionary(String propertyName)
					throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void initializeSuggesterList() throws IOException {

				TRAnalyzingSuggesterExt suggester = new TRAnalyzingInfixSuggesterTest().suggester;
				suggesterList.put(Property.organization, suggester);

			}
		};

		suggester = new Suggester(new SuggesterConfigurationHandler() {

			@Override
			public DictionaryLoader<Lookup> getDictionaryAnalyzer() {
				return dictionaryLoader;
			}
		}, null);

		assertNotNull(suggester);
		try {
			suggester.getSuggesterConfigurationHandler()
					.getDictionaryAnalyzer().initializeSuggesterList();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void mainTest() {

		List<SuggestData> allResults = null;
		try {
			allResults = suggester.lookup(Property.organization, "ALBA IU", 5);
		} catch (Exception e) {
			allResults = null;
		}

		assertNotNull(allResults);

		String suggestion = allResults.get(0).suggestions.get(0).keyword;

		assertEquals("1 Decembrie 1918 University Alba Iulia", suggestion);

	}

}
