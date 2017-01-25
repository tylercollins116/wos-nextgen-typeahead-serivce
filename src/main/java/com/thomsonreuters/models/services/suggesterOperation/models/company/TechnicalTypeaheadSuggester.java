package com.thomsonreuters.models.services.suggesterOperation.models.company;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.suggesterOperation.models.DictionaryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

public class TechnicalTypeaheadSuggester extends Lookup {

	public static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);
	private Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);

	private Analyzer queryAnalyzer = new StandardAnalyzer(
			CharArraySet.EMPTY_SET);

	private TRInfixSuggester suggester = null;

 
	public TechnicalTypeaheadSuggester(InputStream is) {

		suggester = createTechnicalTypeaheadSuggester(is,false);
	}

	/**
	 * 
	 * @param query
	 * @param num
	 * @param condition
	 *            values (1,2,3) 1: will return only prefix match 2: return
	 *            terms matched anywhere 3: first searched for prefix match
	 *            terms, if prefix max terms are not sufficient then other terms
	 *            are searched and merged into result
	 * @return
	 * @throws Exception
	 */

	public SuggestData lookup(String query, int num, int condition,
			boolean includeChild) throws Exception {

		SuggestData suggestData = new SuggestData();

		suggestData.source = "technology";

		/**
		 * 
		 * public List<LookupResult> lookup(CharSequence key, BooleanQuery
		 * contextQuery, int num, int condition, boolean allTermsRequired,
		 * boolean doHighlight) throws IOException {
		 */

		if (condition < 1 || condition > 3) {
			condition = 2;
		}

		if (query == null) {
			return suggestData;
		} else {
			query = query.toLowerCase();
		}

		long start = System.currentTimeMillis();

		List<LookupResult> results = suggester.lookup(query, null, num,
				condition, true, false);

		int count = 0;

		for (LookupResult result : results) {

			// ++count>num because it on top and should break after its size
			// >num
			if (++count > num) {
				break;
			}

			Suggestions suggestions = suggestData.new Suggestions();

			suggestions.keyword = query;

			Map<String, String> map = PrepareDictionary.processJson(new String(
					result.payload.bytes));

			// Term key
			Info info_key = suggestData.new Info();
			info_key.key = "term_string";
			info_key.value = result.key.toString();
			suggestions.info.add(info_key);

			String termCount = map.get("count") != null ? map.get("count")
					: map.get("c");

			if (termCount != null) {
				Info info$ = suggestData.new Info();
				info$.value = termCount;
				info$.key = "term_count";
				suggestions.info.add(info$);
			}

			String inf = map.get("inf") != null ? map.get("inf") : map.get("i");

			if (inf != null) {
				Info info$ = suggestData.new Info();
				info$.value = inf;
				info$.key = "inf";
				suggestions.info.add(info$);
			}
			suggestData.suggestions.add(suggestions);
		}

		suggestData.took = (System.currentTimeMillis() - start) + "";

		return suggestData;
	}

	private TRInfixSuggester createTechnicalTypeaheadSuggester(InputStream is,boolean isTreeStructure) {
		TRInfixSuggester suggester = null;
		try {

			PrepareDictionary dictionary = new PrepareDictionary(is,
					new DictionaryEntry());

			suggester = new TRInfixSuggester(new RAMDirectory(), indexAnalyzer);
			suggester.setTreeStructure(isTreeStructure);
			suggester.build(new TRCompanyEntryIterator(dictionary));

			dictionary.close();
			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	/*********************** This is nothing only to make it Lookup subclass ************/

	@Override
	public long ramBytesUsed() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCount() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void build(InputIterator inputIterator) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<LookupResult> lookup(CharSequence key, Set<BytesRef> contexts,
			boolean onlyMorePopular, int num) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean store(DataOutput output) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean load(DataInput input) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	class TechnicalTermEntry extends Entry {

		public static final String TERM = "keyword";

		private Map<String, String> JsonToMap = null;

		public TechnicalTermEntry() {
		}

		public TechnicalTermEntry(Map<String, String> JsonToMap) {
			setTerm(JsonToMap.remove(TERM));

			// Dont put remove for weight because there is no way to get count
			// on result if we remove that
			setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));

			this.JsonToMap = JsonToMap;

		}

		@Override
		public String getJson() {

			StringBuilder sb = new StringBuilder("{");
			boolean keyWordExist = false;

			if (JsonToMap != null && JsonToMap.size() > 0) {
				Set<String> keys = JsonToMap.keySet();
				for (String key : keys) {

					if (sb.length() > 1) {
						sb.append(",");
					}

					if (key.equalsIgnoreCase("count")) {
						sb.append("\"c\":");
					} else if (key.equalsIgnoreCase("inf")) {
						sb.append("\"i\":");
					} else {
						sb.append("\"" + key + "\":");
					}
					sb.append("\"" + JsonToMap.get(key) + "\"");
				}

			}

			sb.append("}");

			return sb.toString();
		}

		@Override
		public Entry clone(Map<String, String> JsonToMap) {
			return new DictionaryEntry(JsonToMap);
		}
	}

}
