package com.thomsonteuters.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;

public class Suggester {
	private static Suggester instance=null;
	AnalyzingSuggester suggester =null;
	
	private Suggester() throws FileNotFoundException, IOException {
		SimpleAnalyzer analyzer = new SimpleAnalyzer();
		suggester = new FuzzySuggester(analyzer,analyzer, FuzzySuggester.EXACT_FIRST | FuzzySuggester.PRESERVE_SEP, 
				256, -1, true, FuzzySuggester.DEFAULT_MAX_EDITS, FuzzySuggester.DEFAULT_TRANSPOSITIONS,
		         0, FuzzySuggester.DEFAULT_MIN_FUZZY_LENGTH, FuzzySuggester.DEFAULT_UNICODE_AWARE);
		suggester.build(new FileDictionary(new GZIPInputStream(ClassLoader.class.getResourceAsStream("/data/kw.txt.gz"))));

		
	}
	synchronized static Suggester getInstance() {
		if (instance==null) {
			try {
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
