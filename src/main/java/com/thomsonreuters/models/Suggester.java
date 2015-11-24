package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.ESoperation.ArticleESEntry;
import com.thomsonreuters.models.services.ESoperation.IESQueryExecutor;
import com.thomsonreuters.models.services.ESoperation.IQueryGenerator;
import com.thomsonreuters.models.services.ESoperation.PatentESEntry;
import com.thomsonreuters.models.services.ESoperation.PeopleESEntry;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingInfixSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class Suggester implements SuggesterHandler {

	private static final Logger log = LoggerFactory.getLogger(Suggester.class);

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	private final IESQueryExecutor ESQueryExecutor;

	@Inject
	public Suggester(
			SuggesterConfigurationHandler suggesterConfigurationHandler,
			IESQueryExecutor queryExecutor) {
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
		this.ESQueryExecutor = queryExecutor;
	}

	@Override
	public List<SuggestData> lookup(String query, int n) {

		return lookup("wos", query, n);
	}

	@Override
	public List<SuggestData> lookup(String path, String query, int n) {

		long startTime = -1L;

		List<SuggestData> results = new ArrayList<SuggestData>();

		/** These code are execute against ElasticSearch **/

		if (path.equals("article") || path.equals("people")
				|| path.equals("patent")) {

			if (path.equals("article")) {

				try {

					long start = System.currentTimeMillis();

					String returnVaule[] = new String[] {
							"fullrecord.summary.title", "cuid", "fuid" };
					// didn't find cuid in patent fullrecord.summary. and fuid I
					// find it fullrecord.summary.uid but ignored

					HashMap<String, String> aliasField = new HashMap<String, String>(
							1);
					aliasField.put("fullrecord.summary.title", "title");

					IQueryGenerator entry = new ArticleESEntry(returnVaule,
							query, 0, n, "article", aliasField);
					SuggestData data = this.ESQueryExecutor.formatResult(entry);

					data.took = (System.currentTimeMillis() - start) + "";

					results.add(data);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (path.equals("people")) {

				try {
					if (Property.ES_SEARCH_PATH.containsKey("people")) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.country", "institution",
								"role", "fullrecord.summary.authors",
								"fullrecord.summary.uid" };

						HashMap<String, String> aliasField = new HashMap<String, String>(
								2);
						aliasField.put("fullrecord.summary.country", "country");
						aliasField.put("fullrecord.summary.authors", "name");
						aliasField.put("fullrecord.summary.uid", "id");

						IQueryGenerator entry = new PeopleESEntry(returnVaule,
								query, 0, n, "people", aliasField);
						SuggestData data = null;
						try {
							data = this.ESQueryExecutor.formatResult(entry);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						data.took = (System.currentTimeMillis() - start) + "";

						results.add(data);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (path.equals("patent")) {

				try {
					if (Property.ES_SEARCH_PATH.containsKey("patent")) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.patentno",
								"fullrecord.summary.title" };

						HashMap<String, String> aliasField = new HashMap<String, String>(
								2);
						aliasField.put("fullrecord.summary.title", "title");
						aliasField.put("fullrecord.summary.patentno",
								"patentno");

						IQueryGenerator entry = new PatentESEntry(returnVaule,
								query, 0, n, "patent", aliasField);
						SuggestData data = null;
						try {
							data = this.ESQueryExecutor.formatResult(entry);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						data.took = (System.currentTimeMillis() - start) + "";

						results.add(data);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		/** End of codes that execute against ElasticSearch **/

		/** The below codes are execute against Dictionary in S3 bucket **/
		else {

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
									.processJson(new String(
											result.payload.bytes));
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

							Map<String, String> map = PrepareDictionary
									.processJson(new String(
											result.payload.bytes));

							Set<String> keys = map.keySet();

							for (String key : keys) {

								Info info = suggestData.new Info();
								info.key = key;
								info.value = map.get(key);
								suggestions.info.add(info);
							}

							suggestData.suggestions.add(suggestions);

						}

					} catch (Exception e) {
						log.info("cannot find the suggester ");
					}

					suggestData.took = (System.currentTimeMillis() - startTime)
							+ "";

					results.add(suggestData);
				} else if (path.equalsIgnoreCase("topic")) {

					startTime = System.currentTimeMillis();

					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					try {

						for (LookupResult result : ((com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester) suggester)
								.lookup(query, false, n)) {

							Suggestions suggestions = suggestData.new Suggestions();
							suggestions.keyword = result.key.toString();

							Map<String, String> map = PrepareDictionary
									.processJson(new String(
											result.payload.bytes));

							Set<String> keys = map.keySet();

							for (String key : keys) {

								Info info = suggestData.new Info();
								info.key = key;
								info.value = map.get(key);
								suggestions.info.add(info);
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

				suggestData.took = (System.currentTimeMillis() - startTime)
						+ "";
				results.add(suggestData);

			} else if (suggester instanceof AnalyzingInfixSuggester) {

				if (path.equalsIgnoreCase("people")) {

					startTime = System.currentTimeMillis();

					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					try {

						for (LookupResult result : ((AnalyzingInfixSuggester) suggester)
								.lookup(query, false, n)) {

							Map<String, String> map = PrepareDictionary
									.processJson(new String(
											result.payload.bytes));

							Suggestions suggestions = suggestData.new Suggestions();
							suggestions.keyword = "";
							suggestData.suggestions.add(suggestions);

							Info info1 = suggestData.new Info();
							info1.key = "name";
							info1.value = result.key.toString();
							suggestions.info.add(info1);

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

					suggestData.took = (System.currentTimeMillis() - startTime)
							+ "";

					results.add(suggestData);
				} else if (path.equalsIgnoreCase("patent")) {

					startTime = System.currentTimeMillis();

					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					try {

						for (LookupResult result : ((AnalyzingInfixSuggester) suggester)
								.lookup(query, false, n)) {

							Map<String, String> map = PrepareDictionary
									.processJson(new String(
											result.payload.bytes));

							Suggestions suggestions = suggestData.new Suggestions();
							suggestions.keyword = "";
							suggestData.suggestions.add(suggestions);

							Info info1 = suggestData.new Info();
							info1.key = "title";
							info1.value = result.key.toString();
							suggestions.info.add(info1);

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

					suggestData.took = (System.currentTimeMillis() - startTime)
							+ "";

					results.add(suggestData);
				}
			} else if (suggester instanceof TRAnalyzingInfixSuggester) {

				if (path.equalsIgnoreCase("article")) {

					startTime = System.currentTimeMillis();

					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					try {
						for (LookupResult result : ((TRAnalyzingInfixSuggester) suggester)
								.lookup(query, false, n)) {

							Map<String, String> map = PrepareDictionary
									.processJson(new String(
											result.payload.bytes));

							Suggestions suggestions = suggestData.new Suggestions();
							suggestions.keyword = map.remove(Entry.TERM);

							Set<String> keys = map.keySet();

							for (String key : keys) {

								Info info$ = suggestData.new Info();
								info$.key = key;
								info$.value = map.get(key);
								suggestions.info.add(info$);
							}

							Info info$ = suggestData.new Info();
							info$.key = "title";
							info$.value = result.key.toString();
							suggestions.info.add(info$);

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
		}
		/** End of codes that execute against dictionary in S3 Buckets **/
		return results;
	}

	/** added **/
	@Override
	public List<SuggestData> lookup(String query, List<String> sources,
			List<String> infos, int size) {

		List<SuggestData> allSuggestions = new ArrayList<SuggestData>();

		if (sources != null && sources.size() <= 0) {

			/**
			 * This is for ES query property in eiddo starts with "search.path."
			 **/

			Set<String> keysForES = PropertyValue.ES_SEARCH_PATH.keySet();

			/**
			 * -----------------------------------------------------------------
			 **/

			/** This is for dictionary in eiddo starts with "dictionary.path." **/
			Enumeration<String> keysForDictionary = suggesterConfigurationHandler
					.getDictionaryAnalyzer().getSuggesterList().getKeys();

			/**
			 * -----------------------------------------------------------------
			 **/

			Set<String> includeType = new HashSet<String>();

			if (PropertyValue.SELECTED_DEFAULT_TYPEAHEADS != null
					&& PropertyValue.SELECTED_DEFAULT_TYPEAHEADS.length > 0) {
				for (String type : PropertyValue.SELECTED_DEFAULT_TYPEAHEADS) {
					includeType.add(type.toLowerCase().trim());
				}
			}

			boolean defaulTypeExists = (includeType != null && includeType
					.size() > 0);

			// For dictionary

			while (keysForDictionary.hasMoreElements()) {
				String value = (String) keysForDictionary.nextElement();

				if (defaulTypeExists) {
					if (includeType.contains(value.toLowerCase().trim())) {
						sources.add(value);
					}

				} else {
					sources.add(value);
				}
			}

			// for elasticsearch

			for (String value : keysForES) {

				if (defaulTypeExists) {
					if (includeType.contains(value.toLowerCase().trim())) {
						sources.add(value);
					}

				} else {
					sources.add(value);
				}
			}

		}

		for (String path : sources) {
			allSuggestions.addAll(lookup(path, query, size));
		}

		return allSuggestions;
	}

}
