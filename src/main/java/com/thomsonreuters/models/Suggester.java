package com.thomsonreuters.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.IOUtils;

import com.netflix.config.ConfigurationManager;

public class Suggester {

	private static Suggester instance = null;
	private static final String DICTIONARY_PATH_PREFIX = "dictionary.path.";
	private static final String DEFAULT_DICTIONARY = "DEFAULT_DICTIONARY";
	private static final HashMap<String, AnalyzingSuggester> suggesterList = new HashMap<String, AnalyzingSuggester>();

	private static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);
	static {
		try {
			stopSet.addAll(WordlistLoader.getWordSet(IOUtils.getDecodingReader(
					ClassLoader.class
							.getResourceAsStream("/data/profanityWords.txt"),
					StandardCharsets.UTF_8)));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Suggester() throws FileNotFoundException, IOException {
		Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);
		Analyzer queryAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

		defaultDictionary: {
			AnalyzingSuggester suggester = new FuzzySuggester(indexAnalyzer,
					queryAnalyzer);
			suggester.build(prepareDefaultDictionary());
			suggesterList.put(DEFAULT_DICTIONARY, suggester);
		}

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		while (keys.hasNext()) {
			String key = keys.next();

			if (key.startsWith(DICTIONARY_PATH_PREFIX)) {

				String dictionaryPath = ConfigurationManager
						.getConfigInstance().getString(key);

				String endPoint = key.replace(DICTIONARY_PATH_PREFIX, "");

				FileDictionary fileDictionary = null;

				try {
					fileDictionary = prepareDictionary(dictionaryPath);
					AnalyzingSuggester suggester = new FuzzySuggester(
							indexAnalyzer, queryAnalyzer);

					if (fileDictionary != null) {
						suggester.build(fileDictionary);
						suggesterList.put(endPoint, suggester);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

	}

	synchronized static Suggester getInstance() {
		if (instance == null) {
					try {
						instance = new Suggester();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
			 

		}
		return instance;

	}

	public static List<SuggestData> lookup(String query, int n) {
		return lookup("default", query, n);
	}

	public static List<SuggestData> lookup(String path, String query, int n) {
		List<SuggestData> results = new ArrayList<SuggestData>();

		AnalyzingSuggester suggester = null;

		try {

			if ((suggester = getInstance().suggesterList.get(path)) == null
					&& (path != null && path.trim().toLowerCase()
							.equals("default"))) {
				suggester = getInstance().suggesterList.get(DEFAULT_DICTIONARY);
			}

			for (LookupResult result : suggester.lookup(query, false, n)) {
				results.add(new SuggestData(result.key.toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return results;
	}

	private static FileDictionary prepareDictionary(String dictionaryPath)
			throws IOException {

		if (dictionaryPath == null || dictionaryPath.trim().length() <= 0) {
			return null;
		}

		File dictionary = new File(dictionaryPath);
		if (!dictionary.exists()) {
			return null;
		}

		FileDictionary fileDictionary = null;

		if (dictionaryPath.endsWith(".txt")) {
			fileDictionary = new FileDictionary(new FileInputStream(dictionary));
		} else if (dictionaryPath.endsWith(".gz")) {
			fileDictionary = new FileDictionary(new GZIPInputStream(
					new FileInputStream(dictionary)));
		}

		return fileDictionary;
	}

	private FileDictionary prepareDefaultDictionary() throws IOException {

		FileDictionary defaultDictionary = new FileDictionary(
				new GZIPInputStream(
						ClassLoader.class
								.getResourceAsStream("/data/kw.txt.gz")));

		return defaultDictionary;
	}

}
