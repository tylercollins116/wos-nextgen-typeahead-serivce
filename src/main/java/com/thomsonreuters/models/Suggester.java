package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingInfixSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class Suggester implements SuggesterHandler {

	private static final Logger log = LoggerFactory.getLogger(Suggester.class);

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	@Inject
	public Suggester(SuggesterConfigurationHandler suggesterConfigurationHandler) {
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
	}

	@Override
	public List<SuggestData> lookup(String query, int n) {

		return lookup("wos", query, n);
	}

	@Override
	public List<SuggestData> lookup(String path, String query, int n) {

		long startTime = -1L;

		List<SuggestData> results = new ArrayList<SuggestData>();

		Lookup suggester = suggesterConfigurationHandler
				.getDictionaryAnalyzer().getSuggesterList().get(path);

		if (suggester instanceof TRAnalyzingSuggester) {

			if (query.trim().length() < PropertyValue.FUZZTNESS_THRESHOLD) {

				suggester = ((TRFuzzySuggester) suggester).setMaxEdits(0);
			} else {
				suggester = ((TRFuzzySuggester) suggester).setMaxEdits(1);
			}

			if (path.equalsIgnoreCase("categories")) {

				startTime = System.currentTimeMillis();
				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {
					for (LookupResult result : ((TRAnalyzingSuggester) suggester)
							.lookup(query, false, n)) {

						/** output[] **/

						Suggestions suggestions = suggestData.new Suggestions();
						suggestions.keyword = result.key.toString();

						Map<String, String> map = PrepareDictionary
								.processJson(new String(result.payload.bytes));
						Set<String> keys = map.keySet();

						for (String key : keys) {

							Info info$ = suggestData.new Info();
							info$.key = key;
							info$.value = map.get(key);
							suggestions.info.add(info$);
						}

						suggestData.suggestions.add(suggestions);

					}
				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				suggestData.took = (System.currentTimeMillis() - startTime)
						+ "";
				results.add(suggestData);

			} else if (path.equalsIgnoreCase("wos")) {

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {

					for (LookupResult result : ((com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester) suggester)
							.lookup(query, false, n)) {

						Suggestions suggestions = suggestData.new Suggestions();
						suggestions.keyword = result.key.toString();

						System.out.println(result.key.toString() + "\t\t"
								+ result.value);

						suggestData.suggestions.add(suggestions);

					}

				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				suggestData.took = (System.currentTimeMillis() - startTime)
						+ "";

				results.add(suggestData);
			}

		} else if (suggester instanceof TRAnalyzingSuggesterExt) {

			startTime = System.currentTimeMillis();

			SuggestData suggestData = new SuggestData();
			suggestData.source = path;

			List<Map<String, String>> typeSuggestions = new ArrayList<Map<String, String>>();
			try {
				for (LookupResult result : ((TRAnalyzingSuggesterExt) suggester)
						.lookup(query, false, n)) {

					Map<String, String> map = PrepareDictionary
							.processJson(new String(result.payload.bytes));

					Suggestions suggestions = suggestData.new Suggestions();
					suggestions.keyword = map.remove(Entry.TERM);
					suggestData.suggestions.add(suggestions);

					Set<String> keys = map.keySet();

					for (String key : keys) {

						Info info = suggestData.new Info();
						info.key = key;
						info.value = map.get(key);
						suggestions.info.add(info);
					}
				}
			} catch (Exception e) {
				log.info("cannot find the suggester ");
			}

			suggestData.took = (System.currentTimeMillis() - startTime) + "";
			results.add(suggestData);

		} else if (suggester instanceof TRAnalyzingInfixSuggester) {

			if (path.equalsIgnoreCase("article")) {

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {
					for (LookupResult result : ((TRAnalyzingInfixSuggester) suggester)
							.lookup(query, false, n)) {

						Map<String, String> map = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						Suggestions suggestions = suggestData.new Suggestions();
						suggestions.keyword = result.key.toString();

						Set<String> keys = map.keySet();

						for (String key : keys) {

							Info info$ = suggestData.new Info();
							info$.key = key;
							info$.value = map.get(key);
							suggestions.info.add(info$);
						}

						suggestData.suggestions.add(suggestions);

					}
				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				suggestData.took = (System.currentTimeMillis() - startTime)
						+ "";

				results.add(suggestData);

			}
		}

		return results;
	}

	/** added **/
	@Override
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos, int size) {

		List<SuggestData> allSuggestions = new ArrayList<SuggestData>();

		if (sources != null && sources.size() <= 0) {
			Enumeration<String> keys = suggesterConfigurationHandler
					.getDictionaryAnalyzer().getSuggesterList().getKeys();

			while (keys.hasMoreElements()) {
				sources.add((String) keys.nextElement());
			}
		}

		for (String path : sources) {
			allSuggestions.addAll(lookup(path, query, size));
		}

		return allSuggestions;
	}

}
