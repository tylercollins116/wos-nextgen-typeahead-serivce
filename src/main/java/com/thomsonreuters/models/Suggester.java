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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.ESoperation.ESEntry;
import com.thomsonreuters.models.services.ESoperation.IESQueryExecutor;
import com.thomsonreuters.models.services.ESoperation.IQueryGenerator;
import com.thomsonreuters.models.services.suggesterOperation.IProcessPreSearchTerm;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt.Process;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.company.TechnicalTypeaheadSuggester;
import com.thomsonreuters.models.services.suggesters.ProcessPreSearchTerm;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.Property;
 

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
		ElasticEntityProperties eep = suggesterConfigurationHandler
				.getElasticEntityProperties(Property.ENTITY_PREFIX + path);
		/*************************************************************************************/
		/** These code are execute against ElasticSearch **/
		/*************************************************************************************/
		if (eep != null) {
			IQueryGenerator entry = new ESEntry(eep, query, 0, n, path);

			try {
				results.add(getSuggestionsData(entry, eep.getMaxExpansion()));
			} catch (Exception e) {
				e.printStackTrace();
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
				String fuzzyness=ConfigurationManager.getConfigInstance().getString(Property.FUZZTNESS_THRESHOLD);
				
				fuzzyness=fuzzyness==null ?Property.DEFAULT_FUZZTNESS_THRESHOLD+"":fuzzyness;
				
				int fuzzynessLength=10;
				try{
				fuzzynessLength=Integer.parseInt(fuzzyness);
				}catch(Exception e){
					fuzzynessLength=Property.DEFAULT_FUZZTNESS_THRESHOLD;
				}
	 
				if (query.trim().length() < fuzzynessLength) {

					suggester = ((TRFuzzySuggester) suggester).setMaxEdits(0);
				} else {
					suggester = ((TRFuzzySuggester) suggester).setMaxEdits(1);
				}
				
				 
 

					startTime = System.currentTimeMillis();
					SuggestData suggestData = new SuggestData();
					suggestData.source = path;

					/****************************/
					/**** For pre searched Terms **/
					/****************************/

					List<SuggestData.Suggestions> preSearchTerms = null;

					if (preSearchedTermsInfo != null
							&& (preSearchTerms = preSearchedTermsInfo
									.get(path)) != null
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

			}else if (suggester instanceof TechnicalTypeaheadSuggester) {
 
				try {
					results.add(((TechnicalTypeaheadSuggester)suggester).lookup(query, n, 3, false));
				} catch (Exception e) {
					SuggestData suggestData = new SuggestData();					
					suggestData.source="technology";
					suggestData.took=0+"";
					results.add(suggestData);
					log.error("Exception thrown while executing technical typeahead");
					log.info("Exception thrown while executing technical typeahead");
				}

			}
		}
		/***************************************************/
		/** End of codes that execute against dictionary in S3 Buckets **/
		/***************************************************/
		return results;
	}

	/**
	 * lookup suggest list from elastic
	 * 
	 * @param query
	 *            user input word
	 * @param source
	 *            elastic index
	 * @param offset
	 *            elastic offset
	 * @param size
	 *            total size of suggest list
	 * @param uid
	 *            user id
	 * @return suggest data
	 */
	@Override
	public List<SuggestData> lookup(String query, String source, int offset,
			int size, String uid) {

		List<SuggestData> results = new ArrayList<SuggestData>();
		
		//ES query is duplicate have to check and delete it if its unnecessary

		// get avail search index
		Set<String> keysForES = Property.ES_SEARCH_PATH.keySet();
		// validate source index
		if (source != null && source.length() > 0
				&& keysForES.contains(source.toLowerCase())) {
			String path = source.toLowerCase();

			ElasticEntityProperties eep = suggesterConfigurationHandler
					.getElasticEntityProperties(Property.ENTITY_PREFIX + path);

			IQueryGenerator entry = new ESEntry(eep, query, 0, size, path);

			try {
				results.add(getSuggestionsDataWithCount(entry,
						eep.getMaxExpansion()));
			} catch (Exception e) {
				log.error("elastic search error", e);
			}
			
			//ES query is duplicate have to check and delete it if its unnecessary ends
		}else{
			try{
			return (lookup(source,query,size,null));
			}catch(Exception e){
				 log.error("Fail to execute message because of underline error on dictionary based ");
			}
		}
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

			Set<String> keysForES = Property.ES_SEARCH_PATH.keySet();

			/**
			 * -----------------------------------------------------------------
			 **/

			/**
			 * This is for dictionary in eiddo starts with "dictionary.path."
			 **/
			Enumeration<String> keysForDictionary = suggesterConfigurationHandler
					.getDictionaryAnalyzer().getSuggesterList().getKeys();

			/**
			 * -----------------------------------------------------------------
			 **/

			Set<String> includeType = new HashSet<String>();

			 

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
				this.log.info("Grace fully handled time out exception ");
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
						"category", suggestion, 50);

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
						"wos", suggestion, 50);

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
			perSearchedTermsCategories.source = "category";
			perSearchedTermsCategories.suggestions = allCategoriesSuggestions;

			SuggestData perSearchedTerms = new SuggestData();
			perSearchedTerms.source = "wos";
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

	private SuggestData getSuggestionsDataCaller(IQueryGenerator entry,
			int count, Integer[] expansion) throws Exception {

		SuggestData data = this.ESQueryExecutor.formatResult(entry);

		if (data.suggestions.size() <= 0 && count < expansion.length) {
			entry.setMax_expansion(expansion[count]);
			data = getSuggestionsDataCaller(entry, ++count, expansion);
		}
		return data;
	}

	private SuggestData getSuggestionsData(IQueryGenerator entry,
			Integer[] expansion) throws Exception {

		long start = System.currentTimeMillis();

		if (expansion != null && expansion.length > 0) {
			entry.setMax_expansion(expansion[0]);
		}

		SuggestData data = getSuggestionsDataCaller(entry, 0, expansion);
		data.took = (System.currentTimeMillis() - start) + "";
		return data;

	}

	/**
	 * provide suggest list and took is total count of hits
	 * 
	 * @param entry
	 * @param expansion
	 * @return suggest data list
	 * @throws Exception
	 */
	private SuggestData getSuggestionsDataWithCount(IQueryGenerator entry,
			Integer[] expansion) throws Exception {
		SuggestData suggestData = getSuggestionsData(entry, expansion);
		// a temp fix to return total count of hits
		if (suggestData != null) {
			suggestData.took = suggestData.took + ":"
					+ String.valueOf(entry.getTotalCount());
		}
		return suggestData;
	}

}
