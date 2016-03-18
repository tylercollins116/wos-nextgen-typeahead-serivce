package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.thomsonreuters.models.services.ESoperation.PostESEntry;
import com.thomsonreuters.models.services.suggesterOperation.IProcessPreSearchTerm;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingInfixSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt.Process;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesters.ProcessPreSearchTerm;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class Suggester implements SuggesterHandler {

	private static final Logger log = LoggerFactory.getLogger(Suggester.class);

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	private final ExecutorService reloadExecutor = Executors
			.newFixedThreadPool(50);

	private final IProcessPreSearchTerm processPreSearchTerm = new ProcessPreSearchTerm();

	private final IESQueryExecutor ESQueryExecutor;

	@Inject
	public Suggester(
			SuggesterConfigurationHandler suggesterConfigurationHandler,
			IESQueryExecutor queryExecutor) {
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
		this.ESQueryExecutor = queryExecutor;
	}
	
	

	public SuggesterConfigurationHandler getSuggesterConfigurationHandler() {
		return suggesterConfigurationHandler;
	}



	@Override
	public List<SuggestData> lookup(String query, int n) {

		return lookup("wos", query, n);
	}

	@Override
	public List<SuggestData> lookup(String path, String query, int n) {

		return lookup(path, query, n, null);

	}

	public List<SuggestData> lookup(String path, String query, int n,
			Map<String, List<SuggestData.Suggestions>> preSearchedTermsInfo) {
		long startTime = -1L;

		List<SuggestData> results = new ArrayList<SuggestData>();

		/*************************************************************************************/
		/** These code are execute against ElasticSearch **/
		/*************************************************************************************/

		if (path.equals(Property.article) || path.equals(Property.people)
				|| path.equals(Property.patent) || path.equals(Property.post)) {

			if (path.equals(Property.article)) {

				try {

					if (Property.ES_SEARCH_PATH.containsKey(Property.article)) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.title", "cuid", "fuid" };
						/**
						 * didn't find cuid in patent fullrecord.summary. and
						 * fuid I find it fullrecord.summary.uid but ignored
						 **/

						HashMap<String, String> aliasField = new HashMap<String, String>(
								1);
						aliasField.put("fullrecord.summary.title", "title");

						IQueryGenerator entry = new ArticleESEntry(returnVaule,
								query, 0, n, Property.article, aliasField);

						SuggestData data = new SuggestData();
						for (int count = 0; count <= 3; count++) {

							if (data.suggestions.size() <= 0) {
								if (count == 1) {
									entry.setMax_expansion(50);
								} else if (count == 1) {
									entry.setMax_expansion(500);
								} else if (count == 2) {
									entry.setMax_expansion(1500);
								} else if (count == 3) {
									entry.setMax_expansion(4000);
								}

								data = this.ESQueryExecutor.formatResult(entry);

							} else {
								break;
							}

						}

						data.took = (System.currentTimeMillis() - start) + "";
						results.add(data);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (path.equals(Property.people)) {

				try {
					if (Property.ES_SEARCH_PATH.containsKey(Property.people)) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.country", "institution",
								"role", "fullrecord.summary.authors",
								"fullrecord.summary.uid" };

						HashMap<String, String> aliasField = new HashMap<String, String>(
								3);
						aliasField.put("fullrecord.summary.country", "country");
						aliasField.put("fullrecord.summary.authors", "name");
						aliasField.put("fullrecord.summary.uid", "id");

						IQueryGenerator entry = new PeopleESEntry(returnVaule,
								query, 0, n, Property.people, aliasField);
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

			} else if (path.equals(Property.patent)) {

				try {
					if (Property.ES_SEARCH_PATH.containsKey(Property.patent)) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.uid",
								"fullrecord.summary.title",
								"fullrecord.summary.citingsrcscount" };

						HashMap<String, String> aliasField = new HashMap<String, String>(
								3);
						aliasField.put("fullrecord.summary.title", "title");
						aliasField.put("fullrecord.summary.uid", "patentno");
						aliasField.put("fullrecord.summary.citingsrcscount",
								"timeCited");

						IQueryGenerator entry = new PatentESEntry(returnVaule,
								query, 0, n, Property.patent, aliasField);
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

			} else if (path.equals(Property.post)) {

				try {
					if (Property.ES_SEARCH_PATH.containsKey(Property.post)) {

						long start = System.currentTimeMillis();

						String returnVaule[] = new String[] {
								"fullrecord.summary.uid",
								"fullrecord.summary.title",
								"fullrecord.summary.truid",
								"fullrecord.summary.pubdate" };

						HashMap<String, String> aliasField = new HashMap<String, String>(
								4);
						aliasField.put("fullrecord.summary.title", "title");
						aliasField.put("fullrecord.summary.uid", "uid");
						aliasField.put("fullrecord.summary.truid", "truid");
						aliasField.put("fullrecord.summary.pubdate",
								"publishdate");

						IQueryGenerator entry = new PostESEntry(returnVaule,
								query, 0, n, Property.post, aliasField);
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
		/*************************************************************************************/
		/** End of codes that execute against ElasticSearch **/
		/*************************************************************************************/

		/*************************************************************************************/
		/** The below codes are execute against Dictionary in S3 bucket **/
		/*************************************************************************************/

		else {

			Lookup suggester = suggesterConfigurationHandler
					.getDictionaryAnalyzer().getSuggesterList().get(path);

			if (suggester instanceof TRAnalyzingSuggester) {

				if (query.trim().length() < PropertyValue.FUZZTNESS_THRESHOLD) {

					suggester = ((TRFuzzySuggester) suggester).setMaxEdits(0);
				} else {
					suggester = ((TRFuzzySuggester) suggester).setMaxEdits(1);
				}

				if (path.equalsIgnoreCase(Property.category)) {

					startTime = System.currentTimeMillis();
					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					/****************************/
					/**** For pre searched Terms **/
					/****************************/

					List<SuggestData.Suggestions> preSearchTerms = null;

					if (preSearchedTermsInfo != null
							&& (preSearchTerms = preSearchedTermsInfo
									.get(Property.category)) != null
							&& preSearchTerms.size() > 0) {
						suggestData.suggestions.addAll(preSearchTerms);
					} else {
						preSearchTerms = new ArrayList<SuggestData.Suggestions>();
					}

					/************************************/
					/** End of for pre searched Terms **/
					/************************************/

					try {
						for (LookupResult result : ((TRAnalyzingSuggester) suggester)
								.lookup(query, false, n)) {

							/** output[] **/

							Suggestions suggestions = suggestData.new Suggestions();

							suggestions.keyword = result.key.toString();

							if (preSearchTerms.contains(suggestions)) {
								continue;
							}

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

				} else if (path.equalsIgnoreCase(Property.wos)) {

					startTime = System.currentTimeMillis();

					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					/****************************/
					/**** For pre searched Terms **/
					/****************************/

					List<SuggestData.Suggestions> preSearchTerms = null;
					if (preSearchedTermsInfo != null
							&& (preSearchTerms = preSearchedTermsInfo
									.get(Property.wos)) != null

							&& preSearchTerms.size() > 0) {
						suggestData.suggestions.addAll(preSearchTerms);
					} else {
						preSearchTerms = new ArrayList<SuggestData.Suggestions>();
					}

					/************************************/
					/** End of for pre searched Terms **/
					/************************************/

					try {

						for (LookupResult result : ((com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester) suggester)
								.lookup(query, false, n)) {

							Suggestions suggestions = suggestData.new Suggestions();
							suggestions.keyword = result.key.toString();

							if (preSearchTerms.contains(suggestions)) {
								continue;
							}

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
				} else if (path.equalsIgnoreCase(Property.topic)) {

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

					List<LookupResult> allResults = ((TRAnalyzingSuggesterExt) suggester)
							.lookup(query, false, n);
					for (LookupResult result : allResults) {

						Map<String, String> map = PrepareDictionary
								.processJson(new String(
										((TRAnalyzingSuggesterExt) suggester)
												.getReturn(new String(
														result.payload.bytes),
														Process.json)));

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

				if (path.equalsIgnoreCase(Property.people)) {

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
				} else if (path.equalsIgnoreCase(Property.patent)) {

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

				if (path.equalsIgnoreCase(Property.article)) {

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
			List<String> infos, int size, String uid) {

		/***************************************************/
		/** preSearchedTermsInfo will never null **/
		/***************************************************/

		Map<String, List<SuggestData.Suggestions>> preSearchedTermsInfo = new HashMap<String, List<SuggestData.Suggestions>>();

		if (uid != null && uid.trim().length() > 0) {

			List<SuggestData> preSearchedTerms = lookup(query, size, uid, false);

			if (preSearchedTerms != null && preSearchedTerms.size() > 0) {

				for (SuggestData preSearchedTerm : preSearchedTerms) {

					preSearchedTermsInfo.put(preSearchedTerm.source,
							preSearchedTerm.suggestions);

				}

			}

		}

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

		/**
		 * for (String path : sources) {
		 * 
		 * allSuggestions.addAll(lookup(path, query, size));
		 * 
		 * }
		 **/

		List<Future<List<SuggestData>>> furtureList = new ArrayList<Future<List<SuggestData>>>();

		for (String path : sources) {

			Future<List<SuggestData>> future = reloadExecutor
					.submit(new Callable<List<SuggestData>>() {

						@Override
						public List<SuggestData> call() throws Exception {
							return lookup(path, query, size,
									preSearchedTermsInfo);
						}
					});

			furtureList.add(future);

		}

		for (Future<List<SuggestData>> suggestions : furtureList) {
			try {
				allSuggestions.addAll(suggestions.get(1000,
						TimeUnit.MILLISECONDS));
			} catch (InterruptedException | ExecutionException
					| TimeoutException e) {
				e.printStackTrace();
			}
		}

		return allSuggestions;
	}

	/**
	 * 
	 * This is for Pre-Searched Terms
	 * 
	 */

	@Override
	public List<SuggestData> lookup(String query, int size, String uid,
			boolean all) {
		// TODO Auto-generated method stub

		List<SuggestData> allsuggestions = new ArrayList<SuggestData>();
		long startTime = System.currentTimeMillis();

		String[] suggestions = null;

		String[] presearchedTerms = processPreSearchTerm
				.getPreSearchedTerm(uid);

		query = processPreSearchTerm.processAndNormalizeToken(query);

		if (!all) {
			suggestions = processPreSearchTerm.getSuggestions(presearchedTerms,
					query);

			if (suggestions != null && suggestions.length > size) {
				suggestions = Arrays.<String> copyOf(suggestions, size - 1);
			}

			List<SuggestData.Suggestions> allCategoriesSuggestions = new ArrayList<SuggestData.Suggestions>();
			List<SuggestData.Suggestions> allKeywordSuggestions = new ArrayList<SuggestData.Suggestions>();

			for (String suggestion : suggestions) {

				String processedQueryTerm = processPreSearchTerm
						.processAndNormalizeToken(suggestion);

				List<SuggestData> allSuggestdataForCategories = lookup(
						Property.category, suggestion, 50);

				for (SuggestData suggestdata : allSuggestdataForCategories) {
					boolean include = false;

					List<SuggestData.Suggestions> allSuggestDataSuggestions = suggestdata.suggestions;

					for (SuggestData.Suggestions suggestionInfo : allSuggestDataSuggestions) {

						String keyword = suggestionInfo.keyword;
						if (processPreSearchTerm

						.processAndNormalizeToken(keyword).trim()

						.equals(processedQueryTerm)) {
							allCategoriesSuggestions.add(suggestionInfo);
							include = true;
						}

						if (include) {
							break;
						}

					}

				}

				List<SuggestData> allSuggestdataForKeywords = lookup(
						Property.wos, suggestion, 50);

				for (SuggestData suggestdata : allSuggestdataForKeywords) {

					boolean include = false;

					List<SuggestData.Suggestions> allSuggestDataSuggestions = suggestdata.suggestions;

					for (SuggestData.Suggestions suggestionInfo : allSuggestDataSuggestions) {

						String keyword = suggestionInfo.keyword;
						if (processPreSearchTerm
								.processAndNormalizeToken(keyword).trim()
								.equals(processedQueryTerm)) {
							allKeywordSuggestions.add(suggestionInfo);
							include = true;
						}

						if (include) {
							break;
						}

					}

					if (!include) {

						SuggestData.Suggestions tempSuggestions = new SuggestData().new Suggestions();
						tempSuggestions.keyword = suggestion;
						allKeywordSuggestions.add(tempSuggestions);

					}

				}

			}

			/********************* Sorting occurs *****************************/
			Collections.sort(allCategoriesSuggestions, new sortByCount());
			Collections.sort(allKeywordSuggestions, new sortByCount());
			/********************* Sorting Ends *****************************/

			SuggestData perSearchedTermsCategories = new SuggestData();
			perSearchedTermsCategories.source = Property.category;
			perSearchedTermsCategories.suggestions = allCategoriesSuggestions;

			SuggestData perSearchedTerms = new SuggestData();
			perSearchedTerms.source = Property.wos;
			perSearchedTerms.suggestions = allKeywordSuggestions;

			allsuggestions.add(perSearchedTerms);
			allsuggestions.add(perSearchedTermsCategories);

		} else {

			SuggestData data = new SuggestData();
			data.source = "All PreSearchedTerms";
			List<SuggestData.Suggestions> allSuggestions = new ArrayList<SuggestData.Suggestions>();

			for (String keyword : presearchedTerms) {
				SuggestData.Suggestions tempSuggestions = new SuggestData().new Suggestions();
				tempSuggestions.keyword = keyword;
				allSuggestions.add(tempSuggestions);
			}

			data.suggestions = allSuggestions;

			allsuggestions.add(data);

		}

		return allsuggestions;

	}

	private class sortByCount implements Comparator<SuggestData.Suggestions> {

		@Override
		public int compare(Suggestions o1, Suggestions o2) {
			return o2.countToSort().compareTo(o1.countToSort());

		}

	}
}
