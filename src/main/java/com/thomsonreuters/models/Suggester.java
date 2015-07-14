package com.thomsonreuters.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.IOUtils;

public class Suggester {
	private static Suggester instance=null;
	AnalyzingSuggester suggester =null;
	
	
	private static final CharArraySet stopSet = new CharArraySet(StopAnalyzer.ENGLISH_STOP_WORDS_SET, false);  
	static {	
		try {
			stopSet.addAll(WordlistLoader.getWordSet(
					IOUtils.getDecodingReader(ClassLoader.class.getResourceAsStream("/data/profanityWords.txt"), StandardCharsets.UTF_8)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private Suggester() throws FileNotFoundException, IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer(stopSet);
		suggester = new FuzzySuggester(analyzer);
		suggester.build(new FileDictionary(new GZIPInputStream(ClassLoader.class.getResourceAsStream("/data/kw.txt.gz"))));
	}
	synchronized static Suggester getInstance() {
		if (instance==null) {
			try {
				System.out.println("loading...");
				instance=new Suggester();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;

	}
	
	public static List<SuggestData> lookup(String query,int n) {
		List<SuggestData> results =new ArrayList<SuggestData>();
		try {
			for(LookupResult result: getInstance().suggester.lookup(query, false, n)) {
				results.add(new SuggestData(result.key.toString()));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
}
