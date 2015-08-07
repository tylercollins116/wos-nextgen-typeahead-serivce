package com.thomsonreuters.models.services.suggesterOperation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.IOUtils;

public abstract class SuggesterHelper {

	public static final CharArraySet stopSet = new CharArraySet(
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

	private Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);

	private Analyzer queryAnalyzer = new StandardAnalyzer(
			CharArraySet.EMPTY_SET);

	public AnalyzingSuggester createAnalyzingSuggester(InputStream is)
			throws IOException {

		FileDictionary dictionary = (new FileDictionary(
				new BufferedInputStream(is)));

		AnalyzingSuggester suggester = new FuzzySuggester(indexAnalyzer,
				queryAnalyzer);

		suggester.build(dictionary);

		try {
			is.close();
		} catch (Exception e) {
		}

		return suggester;
	}

	public AnalyzingSuggester createDefaultAnalyzingSuggester()
			throws IOException {

		defaultDictionary: {
			AnalyzingSuggester suggester = new FuzzySuggester(indexAnalyzer,
					queryAnalyzer);

			FileDictionary defaultDictionary = new FileDictionary(
					new GZIPInputStream(
							ClassLoader.class
									.getResourceAsStream("/data/kw.txt.gz")));

			suggester.build(defaultDictionary);

			return suggester;

		}

	}

}
