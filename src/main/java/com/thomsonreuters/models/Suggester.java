package com.thomsonreuters.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
	AnalyzingSuggester suggester = null;
	private static String dictionaryPath = null;
	private static final String DICTIONARY_PATH="dictionary.path";

	private static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);
	static {
		try {
			dictionaryPath = ConfigurationManager.getConfigInstance()
					.getString(DICTIONARY_PATH);

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
		suggester = new FuzzySuggester(indexAnalyzer, queryAnalyzer);
		
		
		FileDictionary fileDictionary = null;

		try {
			fileDictionary = prepareDictionary(dictionaryPath);

		} catch (FileNotFoundException filenotFound) {
			fileDictionary = null;
		}

		if (fileDictionary == null) {
			try {
				fileDictionary = prepareDefaultDictionary();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			suggester.build(fileDictionary);
		} catch (Exception unsuccessfulBuild) {
			suggester.build(prepareDefaultDictionary());
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
		List<SuggestData> results = new ArrayList<SuggestData>();
		try {
			for (LookupResult result : getInstance().suggester.lookup(query,
					false, n)) {
				results.add(new SuggestData(result.key.toString()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	private FileDictionary prepareDictionary(String filePath)
			throws IOException {

		if (filePath == null || filePath.trim().length() <= 0) {
			return null;
		}

		File dictionary = new File(filePath);
		if (!dictionary.exists()) {
			return null;
		}

		FileDictionary fileDictionary = null;

		if (filePath.endsWith(".txt")) {
			fileDictionary = new FileDictionary(new FileInputStream(dictionary));
		} else if (filePath.endsWith(".gz")) {
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
