package com.thomsonreuters.models.services.suggesterOperation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.services.suggesterOperation.ext.AnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.FuzzySuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.EntryIterator;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

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

	public AnalyzingSuggesterExt createAnalyzingSuggesterForOrganization(
			InputStream is) {
		AnalyzingSuggesterExt suggester = null;
		try {

			List<Entry> organizationList = PrepareDictionary.initDictonary(is,
					OrganizationEntry.class);

			suggester = new FuzzySuggesterExt(indexAnalyzer, queryAnalyzer);

			suggester.build(new EntryIterator(organizationList.iterator()));

			WeakReference<List<Entry>> weakreference = new WeakReference<List<Entry>>(
					organizationList);
			organizationList = weakreference.get();
			organizationList = null;
			System.gc();
			System.gc();

			 

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	public AnalyzingSuggester createAnalyzingSuggesterForOthers(InputStream is,
			Class enteryClass) {
		AnalyzingSuggester suggester = null;
		try {

			List<Entry> articleList = PrepareDictionary.initDictonary(is,
					enteryClass);

			suggester = new FuzzySuggester(indexAnalyzer, queryAnalyzer);

			suggester.build(new EntryIterator(articleList.iterator()));

			WeakReference<List<Entry>> weakreference = new WeakReference<List<Entry>>(
					articleList);
			articleList = weakreference.get();
			articleList = null;
			System.gc();
			System.gc();

		 

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}
}
