package com.thomsonreuters.models.extact;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggesterConfigurationHandler;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingInfixSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class SuggesterExt implements SuggesterHandlerExt {

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterExt.class);

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	@Inject
	public SuggesterExt(
			SuggesterConfigurationHandler suggesterConfigurationHandler) {
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
	}

	public List<SuggestDataExt> lookup(String query, int n) {

		return lookup("wos", query, n);
	}

	public List<SuggestDataExt> lookup(String path, String query, int n) {

		long startTime = -1L;

		List<SuggestDataExt> jsonarray = new ArrayList<SuggestDataExt>();

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

				List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

				try {
					for (LookupResult result : ((TRAnalyzingSuggester) suggester)
							.lookup(query, false, n)) {

						/** output[] **/

						Map<String, String> suggestions = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						suggestions.put("keyword", result.key.toString());

						allSuggestions.add(suggestions);

					}
				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				String took = (System.currentTimeMillis() - startTime) + "";

				SuggestDataExt actualSuggestions = new SuggestDataExt(path,
						took, allSuggestions);

				jsonarray.add(actualSuggestions);

			} else if (path.equalsIgnoreCase("wos")) {

				List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {

					for (LookupResult result : ((com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester) suggester)
							.lookup(query, false, n)) {

						/** output[] **/

						Map<String, String> suggestions = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						suggestions.put("keyword", result.key.toString());

						allSuggestions.add(suggestions);

					}

				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				String took = (System.currentTimeMillis() - startTime) + "";

				SuggestDataExt actualSuggestions = new SuggestDataExt(path,
						took, allSuggestions);

				jsonarray.add(actualSuggestions);
			}

		} else if (suggester instanceof TRAnalyzingSuggesterExt) {

			List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

			startTime = System.currentTimeMillis();

			SuggestData suggestData = new SuggestData();
			suggestData.source = path;

			List<Map<String, String>> typeSuggestions = new ArrayList<Map<String, String>>();
			try {
				for (LookupResult result : ((TRAnalyzingSuggesterExt) suggester)
						.lookup(query, false, n)) {

					Map<String, String> suggestions = PrepareDictionary
							.processJson(new String(result.payload.bytes));

					allSuggestions.add(suggestions);
				}
			} catch (Exception e) {
				log.info("cannot find the suggester ");
			}

			String took = (System.currentTimeMillis() - startTime) + "";

			SuggestDataExt actualSuggestions = new SuggestDataExt(path, took,
					allSuggestions);

			jsonarray.add(actualSuggestions);
		} else if (suggester instanceof AnalyzingInfixSuggester) {

			if (path.equalsIgnoreCase("people")) {

				List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {

					for (LookupResult result : ((AnalyzingInfixSuggester) suggester)
							.lookup(query, false, n)) {

						Map<String, String> suggestions = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						suggestions.put("name", result.key.toString());

						allSuggestions.add(suggestions);

					}

				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				String took = (System.currentTimeMillis() - startTime) + "";

				SuggestDataExt actualSuggestions = new SuggestDataExt(path,
						took, allSuggestions);

				jsonarray.add(actualSuggestions);
			} else if (path.equalsIgnoreCase("patent")) {

				List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {

					for (LookupResult result : ((AnalyzingInfixSuggester) suggester)
							.lookup(query, false, n)) {

						Map<String, String> suggestions = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						suggestions.put("title", result.key.toString());

						allSuggestions.add(suggestions);

					}

				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				String took = (System.currentTimeMillis() - startTime) + "";

				SuggestDataExt actualSuggestions = new SuggestDataExt(path,
						took, allSuggestions);

				jsonarray.add(actualSuggestions);
			}
		} else if (suggester instanceof TRAnalyzingInfixSuggester) {

			if (path.equalsIgnoreCase("article")) {

				List<Map<String, String>> allSuggestions = new ArrayList<Map<String, String>>();

				startTime = System.currentTimeMillis();

				SuggestData suggestData = new SuggestData();
				suggestData.source = path;

				try {
					for (LookupResult result : ((TRAnalyzingInfixSuggester) suggester)
							.lookup(query, false, n)) {
						Map<String, String> suggestions = PrepareDictionary
								.processJson(new String(result.payload.bytes));

						suggestions.put("title", result.key.toString());

						allSuggestions.add(suggestions);

					}
				} catch (Exception e) {
					log.info("cannot find the suggester ");
				}

				String took = (System.currentTimeMillis() - startTime) + "";

				SuggestDataExt actualSuggestions = new SuggestDataExt(path,
						took, allSuggestions);

				jsonarray.add(actualSuggestions);
			}
		}

		return jsonarray;
	}

	/** added **/
	@Override
	public List<SuggestDataExt> lookup(String query, List<String> sources,
			List<String> infos, int size) {

		List<SuggestDataExt> allSuggestions = new ArrayList<SuggestDataExt>();

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
