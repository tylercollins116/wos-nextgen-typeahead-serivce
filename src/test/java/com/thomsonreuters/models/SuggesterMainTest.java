package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Before;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;


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

				TRAnalyzingSuggesterExt suggester = new TRAnalyzingSuggesterExtTest().suggester;
				suggesterList.put("organization", suggester);

				TRAnalyzingSuggester suggester1 = new TRAnalyzingSuggesterTest().suggester;

				suggesterList.put("topic", suggester1);
				suggesterList.put("category", suggester1);
				suggesterList.put("wos", suggester1);

			}
		};

		suggester = new Suggester(new SuggesterConfigurationHandler() {

			@Override
			public DictionaryLoader<Lookup> getDictionaryAnalyzer() {
				return dictionaryLoader;
			}

			@Override
			public ElasticEntityProperties getElasticEntityProperties(String esPath) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<String> getRegisteredElasticEntityNames() {
				// TODO Auto-generated method stub
				return null;
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
			allResults = suggester.lookup("organization", "ALBA IU", 5, false);
		} catch (Exception e) {
			allResults = null;
		}

		assertNotNull(allResults);

		String suggestion = allResults.get(0).suggestions.get(0).keyword;

		assertEquals("1 Decembrie 1918 University Alba Iulia", suggestion);

		/************************* For topic Unit Test **********/
		allResults = null;
		try {

			allResults = suggester.lookup("topic", "scr", 5, false);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

			allResults = suggester.lookup("category", "scr", 5, false);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

			allResults = suggester.lookup("wos", "scr", 5, false);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

		} catch (Exception e) {
			allResults = null;
		}

		try {
			List<String> sources = Arrays.asList(new String[] { "wos",
					"organization", "category", "topic" });
			suggester.lookup("scr", sources, new ArrayList<String>(), 4, null, false);

			suggester.lookup("scr", 4, null, true, false);

		} catch (Exception e) {
		}

	}

}
