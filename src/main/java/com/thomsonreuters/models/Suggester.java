package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;


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

		List<SuggestData> results = new ArrayList<SuggestData>();

		AnalyzingSuggester suggester = suggesterConfigurationHandler.getDictionaryAnalyzer()
				.getSuggesterList().get(path);

		try {
			for (LookupResult result : suggester.lookup(query, false, n)) {
				results.add(new SuggestData(result.key.toString()));
			}
		} catch (Exception e) {
			log.info("cannot find the suggester ");
		}

		return results;
	}
	
	/**added**/
	@Override
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos) {
		 
		List<SuggestData> allSuggestions = new ArrayList<SuggestData>();

		if (query != null && query.trim().length() > 0) {

			SuggestData suggestion = new SuggestData(query);
			allSuggestions.add(suggestion);
		}

		if (sources != null && sources.size() > 0) {

			for (String suggest : sources) {
				SuggestData suggestion = new SuggestData(suggest);
				allSuggestions.add(suggestion);

			}

		}

		if (infos != null && infos.size() > 0) {

			for (String suggest : infos) {
				SuggestData suggestion = new SuggestData(suggest);
				allSuggestions.add(suggestion);

			}

		}

		return allSuggestions;
	}


}
