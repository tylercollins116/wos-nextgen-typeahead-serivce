package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Before;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;
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

				TRAnalyzingSuggesterExt suggester = new TRAnalyzingSuggesterExtTest().suggester;
				suggesterList.put(Property.organization, suggester);

				TRAnalyzingSuggester suggester1 = new TRAnalyzingSuggesterTest().suggester;

				suggesterList.put(Property.topic, suggester1);
				suggesterList.put(Property.category, suggester1);
				suggesterList.put(Property.wos, suggester1);

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

		/************************* For topic Unit Test **********/
		allResults = null;
		try {

			allResults = suggester.lookup(Property.topic, "scr", 5);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

			allResults = suggester.lookup(Property.category, "scr", 5);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

			allResults = suggester.lookup(Property.wos, "scr", 5);
			assertNotNull(allResults);
			suggestion = allResults.get(0).suggestions.get(0).keyword;
			assertEquals("scrapie", suggestion);

		} catch (Exception e) {
			allResults = null;
		}

		try {
			List<String> sources = Arrays.asList(new String[] { Property.wos,
					Property.organization, Property.category, Property.topic });
			suggester.lookup("scr", sources, new ArrayList<String>(), 4, null);

			suggester.lookup("scr", 4, null, true);

		} catch (Exception e) {
		}

	}

}