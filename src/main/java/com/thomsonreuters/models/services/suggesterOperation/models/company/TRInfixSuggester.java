package com.thomsonreuters.models.services.suggesterOperation.models.company;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.SortingMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.EarlyTerminatingSortingCollector;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jettison.json.JSONObject;

import com.thomsonreuters.models.services.suggesterOperation.models.Entry;

public class TRInfixSuggester implements Closeable {

	/** Field name used for the indexed text. */
	protected final static String TEXT_FIELD_NAME = "text";
	private boolean isTreeStructure = true;
	/**
	 * Field name used for the indexed text, as a StringField, for exact lookup.
	 */
	protected final static String EXACT_TEXT_FIELD_NAME = "exacttext";
	protected final static String ID_TEXT_FIELD_NAME = "idfield";
	protected final static String PARENTS_TEXT_FIELD_NAME = "parentfield";
	private static final Sort SORT = new Sort(new SortField("weight",
			SortField.Type.LONG, true));

	private FieldType storeFieldType = new FieldType();
	private FieldType indexFieldType = new FieldType();

	private Document document = new Document();
	private IndexWriter writer;

	private Field weightField = new NumericDocValuesField("weight", 0L);
	private Field payloadField = null;
	private Field termField = null;
	private Field exactTextField = new StringField(EXACT_TEXT_FIELD_NAME, "",
			Field.Store.NO);
	private Field parentTextField = new StringField(PARENTS_TEXT_FIELD_NAME,
			"", Field.Store.NO);
	private Field idTextField = new StringField(ID_TEXT_FIELD_NAME, "",
			Field.Store.NO);

	private PhraseTokenizerTokens tokenizer = new PhraseTokenizerTokens();

	protected SearcherManager searcherMgr;

	private Directory dir = null;

	public TRInfixSuggester(Directory dir, Analyzer analyzer) throws Exception {

		storeFieldType.setOmitNorms(true);
		storeFieldType.setIndexOptions(IndexOptions.NONE);
		storeFieldType.setStored(true);
		storeFieldType.setTokenized(false);

		indexFieldType.setOmitNorms(true);
		// indexFieldType.setIndexed(true);
		indexFieldType
				.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
		indexFieldType.setStored(false);

		payloadField = new Field("payload", new byte[0], storeFieldType);
		termField = new Field("term", new byte[0], storeFieldType);

		document.add(weightField);
		document.add(payloadField);
		document.add(exactTextField);
		document.add(termField);
		document.add(new Field(TEXT_FIELD_NAME, tokenizer, indexFieldType));
		if (isTreeStructure) {
			document.add(parentTextField);
			document.add(idTextField);
		}

		if (dir == null) {
			dir = new RAMDirectory();
		} else {
			this.dir = dir;
		}

		// Already built; open it:
		writer = getIndexer();

	}

	public void setTreeStructure(boolean isTreeStructure) {
		this.isTreeStructure = isTreeStructure;
	}

	private IndexWriter getIndexer() throws Exception {
		IndexWriterConfig iwc = new IndexWriterConfig(DummyAnalyzer.INSTANCE);
		iwc.setOpenMode(OpenMode.CREATE);

		iwc.setRAMBufferSizeMB(512.0);
		iwc.setMergePolicy(new SortingMergePolicy(iwc.getMergePolicy(), SORT));

		IndexWriter indexWriter = new IndexWriter(this.dir, iwc);
		return indexWriter;

	}

	public void build(InputIterator iter) throws IOException {

		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		} catch (Exception e) {
			// Ignore error no need to handle it here
		}

		boolean success = false;
		try {

			writer = getIndexer();

			BytesRef text;
			while ((text = iter.next()) != null) {
				BytesRef payload;
				if (iter.hasPayloads()) {
					payload = iter.payload();
				} else {
					payload = null;
				}

				buildDocument(text, iter.weight(), payload);
				writer.addDocument(document);
			}

			writer.commit();
			// writer.close();
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		} finally {
			if (success == false && writer != null) {
				writer.rollback();
				writer = null;
			}
		}
	}

	private void buildDocument(BytesRef text, long weight, BytesRef payload)
			throws IOException {
		String textString = text.utf8ToString();

		String payloads = new String(payload.bytes);
		String processedTextString = processeTextForExactMatch(textString);
		exactTextField.setStringValue(processedTextString);
		weightField.setLongValue(weight);
		tokenizer.setText(textString);
		termField.setBytesValue(text.bytes);
		if (payload != null) {

			payloadField.setBytesValue(payload.bytes);

			if (isTreeStructure) {

				String id = new String(payload.bytes).split(Entry.DELIMETER)[0];
				if (id != null) {
					idTextField.setStringValue(processeTextForExactMatch(id));

				}

				try {
					String json = getReturn(new String(payload.bytes),
							Process.json);

					JSONObject oject = new JSONObject(json);
					String parent = oject.getString("parents");
					parentTextField.setStringValue("");

					if (parent != null && (parent = parent.trim()).length() > 0
							&& !parent.equals("[]")) {

						parentTextField
								.setStringValue(processeTextForExactMatch(parent));

					}
				} catch (Exception e) {
					// dont do anything if its a ultimate parent it reaches here
				}

			}

		}

	}

	public List<LookupResult> lookup(CharSequence key,
			BooleanQuery contextQuery, int num, int condition,
			boolean allTermsRequired, boolean doHighlight) throws Exception {

		int realNum = num;
		// num = num + 1000;//When varients is included then we have to increase
		// th bucket size
		// num = num + 500;

		if (isTreeStructure) {
			num = num + 500;
		}

		/**
		 * condition
		 * 
		 * values (1,2,3)
		 * 
		 * 1: will return only prefix match
		 * 
		 * 2: return terms matched anywhere
		 * 
		 * 3: first searched for prefix match terms, if prefix max terms are not
		 * sufficient then other terms are searched and merged into result
		 */

		if (searcherMgr == null) {
			searcherMgr = new SearcherManager(this.dir, new SearcherFactory());
			// throw new IllegalStateException("suggester was not built");
		}

		final BooleanClause.Occur occur;
		if (allTermsRequired) {
			occur = BooleanClause.Occur.MUST;
		} else {
			occur = BooleanClause.Occur.SHOULD;
		}

		BooleanQuery query;
		Set<String> matchedTokens;
		String prefixToken = null;

		BooleanQuery pregixMatchedQuery = new BooleanQuery();

		pregixMatchedQuery.add(new PrefixQuery(new Term(EXACT_TEXT_FIELD_NAME,
				processeTextForExactMatch(key.toString()))), Occur.MUST);

		Query finalQuery = null;
		BooleanQuery finalQuerysec = null;

		if (condition == 1) {
			finalQuery = finishQuery(pregixMatchedQuery, allTermsRequired);
		}

		if (condition == 2) {

			PhraseTokenizerTokens tokenizer = new PhraseTokenizerTokens();

			tokenizer.setText(key.toString());
			tokenizer.reset();
			List<String> words = tokenizer.getTokenizedWords();
			List<SpanQuery> spanQueryList = new ArrayList<SpanQuery>();
			for (int i = 0; i < words.size(); i++) {
				if (i < words.size() - 1) {
					spanQueryList.add(new SpanTermQuery(new Term(
							TEXT_FIELD_NAME, words.get(i))));
				} else {

					spanQueryList.add(new SpanMultiTermQueryWrapper(
							new PrefixQuery(new Term(TEXT_FIELD_NAME, words
									.get(i)))));
				}

			}

			SpanQuery finalSpanQuery = null;

			if (spanQueryList.size() > 1) {
				finalSpanQuery = new SpanNearQuery(
						spanQueryList.toArray(new SpanQuery[] {}), 1, true);
			} else {

				finalSpanQuery = spanQueryList.get(0);
			}

			BooleanQuery anywherematched = new BooleanQuery();
			anywherematched.add(finalSpanQuery, Occur.MUST);

			finalQuery = finishQuery(anywherematched, allTermsRequired);
		}

		if (condition == 3) {

			finalQuery = finishQuery(pregixMatchedQuery, allTermsRequired);
			finalQuerysec = new BooleanQuery();
			finalQuerysec.add(new PrefixQuery(new Term(TEXT_FIELD_NAME,
					processeTextForExactMatch(key.toString()))), Occur.MUST);

		}

		// System.out.println("finalQuery=" + query);

		// Sort by weight, descending:
		TopFieldCollector c = TopFieldCollector.create(SORT, num, true, false,
				false);

		TopFieldCollector c2 = TopFieldCollector.create(SORT, num, true, false,
				false);

		final MergePolicy mergePolicy = writer.getConfig().getMergePolicy();
		Collector c3 = new EarlyTerminatingSortingCollector(c2, SORT, num,
				(SortingMergePolicy) mergePolicy);

		// We sorted postings by weight during indexing, so we

		IndexSearcher searcher = searcherMgr.acquire();

		List<LookupResult> results = null;
		List<LookupResult> results1 = null;
		List<LookupResult> results2 = null;

		try {

			searcher.search(finalQuery, c);

			TopDocs hits = c.topDocs();

			results1 = createResults(searcher, hits, num, key, prefixToken);

			if (finalQuerysec != null) {
				searcher.search(finalQuerysec, c3);

				TopDocs hits_sec = c2.topDocs();

				// Slower way if postings are not pre-sorted by weight:
				// hits = searcher.search(query, null, num, SORT);
				results2 = createResults(searcher, hits_sec, num, key,
						prefixToken);
			}

		} finally {
			searcherMgr.release(searcher);
		}

		results = mergeResultSet(results1, results2, realNum);

		// System.out.println((System.currentTimeMillis() - t0) +
		// " msec for infix suggest");
		// System.out.println(results);

		return results;
	}

	public List<LookupResult> lookForParentOrChild(String queryString,
			boolean isParent) throws IOException {

		String prefixToken = null;
		Set<String> matchedTokens = new HashSet<String>();

		int num = 1;

		if (!isParent) {
			num = 1000;
		}

		TopFieldCollector c = TopFieldCollector.create(SORT, num, true, false,
				false);

		// We sorted postings by weight during indexing, so we
		// only retrieve the first num hits now:
		final MergePolicy mergePolicy = writer.getConfig().getMergePolicy();
		Collector c2 = new EarlyTerminatingSortingCollector(c, SORT, num,
				(SortingMergePolicy) mergePolicy);
		IndexSearcher searcher = searcherMgr.acquire();

		List<LookupResult> results = null;

		String field = null;
		if (isParent) {
			field = ID_TEXT_FIELD_NAME;
		} else {
			field = PARENTS_TEXT_FIELD_NAME;

		}

		Term term = new Term(field, processeTextForExactMatch(queryString));
		Query query = new TermQuery(term);

		try {
			// System.out.println("got searcher=" + searcher);
			searcher.search(query, c2);

			TopDocs hits = c.topDocs();

			// Slower way if postings are not pre-sorted by weight:
			// hits = searcher.search(query, null, num, SORT);
			results = createResults(searcher, hits, num + 100, queryString,
					prefixToken);
		} catch (Exception e) {

		} finally {
			searcherMgr.release(searcher);
		}

		return results;

	}

	public List<LookupResult> mergeResultSet(List<LookupResult> results1,
			List<LookupResult> results2, int num) {
		List<LookupResult> results = new ArrayList<LookupResult>();
		final Set<String> globalSeen = new HashSet<String>();

		if (isTreeStructure) {
			num = num * 10;
		}

		for (LookupResult result : results1) {

			String alias = getReturn(new String(result.payload.bytes),
					Process.keyword);

			if (!globalSeen.contains(alias)) {
				globalSeen.add(alias);
				results.add(result);
			}

			if (results.size() >= num) {
				break;
			}
		}

		if (results2 != null && results2.size() > 0 && results.size() < num) {
			for (LookupResult result : results2) {

				String alias = getReturn(new String(result.payload.bytes),
						Process.keyword);

				if (!globalSeen.contains(alias)) {
					globalSeen.add(alias);
					results.add(result);
				}

				if (results.size() == num) {
					break;
				}
			}

		}
		return results;

	}

	protected List<LookupResult> createResults(IndexSearcher searcher,
			TopDocs hits, int num, CharSequence charSequence, String prefixToken)
			throws IOException {

		List<LookupResult> results = new ArrayList<>();
		/** to remove duplicate **/
		final Set<String> globalSeen = new HashSet<String>();
		HashMap<String, List<Record>> allRecords = new HashMap<String, List<Record>>();
		/** **/

		int totalhits = hits.scoreDocs.length > num ? num
				: hits.scoreDocs.length;

		for (int seq_ = 0; seq_ < totalhits; seq_++) {
			FieldDoc fd = (FieldDoc) hits.scoreDocs[seq_];
			long score = (Long) fd.fields[0];
			ScoreDoc scoreDoc = hits.scoreDocs[seq_];
			int docID = scoreDoc.doc;
			Document doc = searcher.doc(docID);
			BytesRef payload = null;

			try {
				payload = doc.getBinaryValue("payload");
			} catch (Exception e) {
				// need to handle , paylod is optional
			}

			String text = new String(doc.getBinaryValue("term").bytes);
			LookupResult result = new LookupResult(text, score, payload, null);
			String alias = null;
			if (payload != null) {
				alias = getReturn(new String(result.payload.bytes),
						Process.keyword);
			}

			/** new code **/

			try {

				List<Record> allAlias = allRecords.get(alias);
				if (allAlias == null) {
					allAlias = new ArrayList<TRInfixSuggester.Record>();
				}
				allAlias.add(new Record(text, alias, result));

				allRecords.put(alias, allAlias);

			} catch (Exception e) {
				e.printStackTrace();
			}

			/** new code Ends here **/

			if (!globalSeen.contains(alias)) {
				globalSeen.add(alias);
				results.add(result);
			}

			/**
			 * Manoj Manandhar
			 * 
			 * comment no 1000
			 * 
			 * break is removed and added following lines
			 * 
			 * 
			 */
			if (results.size() == num) {
				break;
			}

		}

		List<LookupResult> tmpResults = new ArrayList<>();

		boolean hasAlisaWithCount = false;

		Set<String> keys = allRecords.keySet();
		for (String key : keys) {
			Record record = null;
			List<Record> allAlias = allRecords.get(key);

			int rcount = 0;
			if (allAlias.size() > 0) {
				int count = 0;
				for (Record data : allAlias) {
					String name = data.text;
					String[] info = name.split("\\^");

					if (info.length > 1) {
						try {
							count = Integer.parseInt(info[1]);
							hasAlisaWithCount = true;
						} catch (Exception e) {
							count = 0;
						}

						if (rcount < count) {
							rcount = count;
							record = data;
						}

					} else {
						record = data;
						break;
					}
				}

				if (record != null) {
					tmpResults.add(record.result);
					// System.out.println("Adding "+record.text);
					if (tmpResults.size() == num) {
						break;
					}
				}
			}
		}

		if (hasAlisaWithCount && tmpResults.size() > 0) {
			results.clear();
			results.addAll(tmpResults);
		}

		return results;
	}

	protected Query finishQuery(BooleanQuery in, boolean allTermsRequired) {
		return in;
	}

	private void resetField() {
		weightField.setLongValue(0L);
		payloadField.setBytesValue(new byte[] {});
		exactTextField.setStringValue("");
		parentTextField.setStringValue("");
		idTextField.setStringValue("");
	}

	protected String getReturn(String data, Process process) {

		String[] result = data.split(Entry.DELIMETER);
		if (process == process.json) {
			return result[1];
		} else if (process == process.keyword) {
			return result[0].trim().toLowerCase();
		} else {
			return "";
		}

	}

	private String processeTextForExactMatch(String text) {

		StringBuilder processedString = new StringBuilder();
		char[] chars = text.toCharArray();

		int space = 0;
		for (char c : chars) {
			if (c == ' ') {
				++space;

				if (space <= 1) {
					c = '_';
					processedString.append(c);
				}
				continue;
			}
			space = 0; // This is to remove multiple space

			if (c == '[' || c == ']' || c == '"' || c == '.' || c == ','
					|| c == '`' || c == '?' || c == '|' || c == '~' || c == '!'
					|| c == '@' || c == '^' || c == '#' || c == '$') {
				continue;
			}
			processedString.append(c);
		}
		return processedString.toString().toLowerCase();
	}

	/******************************* MY Class starts here **************************************************/

	class Record {
		String text;
		String alias;
		LookupResult result;

		public Record(String text, String alias, LookupResult result) {
			this.result = result;
			this.text = text;
			this.alias = alias;
		}

	}

	public static final class DummyAnalyzer extends Analyzer {
		public static final DummyAnalyzer INSTANCE = new DummyAnalyzer();

		/**
		 * @Override protected TokenStreamComponents createComponents(String
		 *           fieldName, Reader reader) { // TODO Auto-generated method
		 *           stub return null; }
		 **/
		@Override
		protected TokenStreamComponents createComponents(String fieldName) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	public class PhraseTokenizerTokens extends TokenStream {

		// This is main part and indexing is the value hold by termAtt
		private final CharTermAttribute termAtt;

		private final PositionIncrementAttribute positionAtt;

		private List<String> tokenizedWords = new ArrayList<String>();

		private String text = "";

		private int index = 0;

		public PhraseTokenizerTokens() {

			termAtt = addAttribute(CharTermAttribute.class);
			positionAtt = addAttribute(PositionIncrementAttribute.class);
		}

		public List<String> getTokenizedWords() {
			return this.tokenizedWords;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		@Override
		public boolean incrementToken() throws IOException {

			this.termAtt.setEmpty();

			if (tokenizedWords.size() > 0) {
				this.termAtt.append(tokenizedWords.remove(0));
				positionAtt.setPositionIncrement(1);
				return true;
			} else {
				tokenizedWords.clear();
				return false;
			}
		}

		public String getToken() {
			return termAtt.toString();

		}

		// This reset function is called automatically for the single time at
		// the
		// beginning of each document( whenever the operation starts this is
		// called for single time only and if we have to change the value of
		// list
		// then we need to call it again by ourself)

		@Override
		public void reset() throws IOException {
			index = 0;
			chunk(text);
		}

		private void chunk(String data) {
			text = validateTerm(text.toLowerCase());
			normalizedPhrase(text);
		}

		private String validateTerm(String term) {

			char[] data = term.toCharArray();
			StringBuilder sb = new StringBuilder();

			for (char c : data) {

				if (c == '[' || c == ']' || c == '"' || c == '.' || c == ','
						|| c == '`' || c == '?' || c == '|' || c == '~'
						|| c == '!' || c == '@' || c == '^' || c == '#'
						|| c == '$' || c == '.' || c == '/' || c == '-') {

					sb.append(' ');
					continue;
				}

				if (Character.isAlphabetic(c) || Character.isDigit(c)
						|| c == ' ') {
					sb.append(c);
				}
			}

			return sb.toString();
		}

		private void normalizedPhrase(String stringToTokenize) {

			if (stringToTokenize == null) {
				return;
			}

			StringTokenizer tokenizer = new StringTokenizer(stringToTokenize);

			while (tokenizer.hasMoreElements()) {

				tokenizedWords.add(tokenizer.nextToken());

			}

		}

	}

	@Override
	public void close() throws IOException {
		if (searcherMgr != null) {
			searcherMgr.close();
			searcherMgr = null;
		}
		if (writer != null) {
			writer.close();
			dir.close();
			writer = null;
		}

	}

}
