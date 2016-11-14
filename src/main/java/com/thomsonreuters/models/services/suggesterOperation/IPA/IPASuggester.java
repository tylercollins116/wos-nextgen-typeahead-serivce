package com.thomsonreuters.models.services.suggesterOperation.IPA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggesterConfigurationHandler;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt.Process;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.company.CompanyTypeaheadSuggester;
import com.thomsonreuters.models.services.util.PrepareDictionary;

@Singleton
public class IPASuggester implements IPASuggesterHandler {

	private static final Logger log = LoggerFactory
			.getLogger(IPASuggester.class);

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	@Inject
	public IPASuggester(
			SuggesterConfigurationHandler suggesterConfigurationHandler) {
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
	}

	public String lookup(String path, String query, int n) {

		long startTime = -1L;

		String JsonString = "";
		JSONObject root = new JSONObject();

		Lookup suggester = suggesterConfigurationHandler
				.getDictionaryAnalyzer().getSuggesterList().get(path);

		if (suggester instanceof CompanyTypeaheadSuggester) {
			try {
				JsonString = ((CompanyTypeaheadSuggester) suggester).lookup(
						query, n, 2);
			} catch (Exception e) {
				e.printStackTrace();
				JsonString = "{}";
			}

		} else

		if (suggester instanceof TRAnalyzingSuggesterExt) {

			startTime = System.currentTimeMillis();

			SuggestData suggestData = new SuggestData();
			suggestData.source = path;

			List<JSONObject> allSuggestions = new ArrayList<JSONObject>();

			try {
				for (LookupResult result : ((TRAnalyzingSuggesterExt) suggester)
						.lookup(query, false, n)) {

					JSONObject suggestion = new JSONObject();
					Map<String, String> suggestions = PrepareDictionary
							.processJson(new String(
									((TRAnalyzingSuggesterExt) suggester)
											.getReturn(new String(
													result.payload.bytes),
													Process.json)));

					Set<String> keys = suggestions.keySet();

					for (String key : keys) {

						String value = suggestions.get(key);
						if (key.equalsIgnoreCase("children")
								|| key.equalsIgnoreCase("parents")) {
							if (value.indexOf(Entry.DELIMETER) > 0) {
								suggestion.put(key, Arrays.asList(value
										.split(Entry.DELIMETER)));
							} else {
								if (value != null && value.length() > 0) {
									suggestion.put(key, Arrays
											.asList(new String[] { value }));
								} else {

									suggestion.put(key,
											Arrays.asList(new String[] {}));

								}
							}
						} else {
							if (key.equalsIgnoreCase("keyword")) {
								key = "id";
							}
							if (key.equalsIgnoreCase("count")) {
								continue;
							}
							suggestion.put(key, value);
						}
					}
					allSuggestions.add(suggestion);
				}

				root.put("source", path);
				root.put("Suggestions", allSuggestions);

			} catch (Exception e) {
				log.info("cannot find the suggester ");
			}

			String took = (System.currentTimeMillis() - startTime) + "";

			JsonString = root.toString();

		}

		return JsonString;
	}

	/** added **/

}
