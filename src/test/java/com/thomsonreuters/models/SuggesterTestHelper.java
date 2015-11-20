package com.thomsonreuters.models;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.util.BytesRef;

 

public class SuggesterTestHelper {
	
	public AnalyzingSuggester getAnalyzingSuggester() {

		List<String> badWords = Arrays.asList("fuck", "anal", "bitch ", "ass",
				"ass-fucker", "asses", "assfucker", "assfukka", "asshole",
				"assholes", "asswhole", "a_s_s", "b!tch", "b00bs", "b17ch",
				"b1tch", "ballbag", "ballsack", "wachau", "bastard",
				"beastial", "beastiality", "bellend", "bestial", "bestiality",
				"bi+ch", "biatch");

		CharArraySet stopSet = new CharArraySet(CharArraySet.EMPTY_SET, false);

		stopSet.addAll(badWords);

		Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);

		Analyzer queryAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

		AnalyzingSuggester suggester = new FuzzySuggester(indexAnalyzer,
				queryAnalyzer);

		org.apache.lucene.search.spell.Dictionary dictionary = new org.apache.lucene.search.spell.Dictionary() {

			@Override
			public InputIterator getEntryIterator() throws IOException {
				// TODO Auto-generated method stub
				return new SuggesterIterator(getSuggestions().iterator());
			}
		};

		try {
			suggester.build(dictionary);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return suggester;

	}

	private List<Suggest> getSuggestions() {
		List<Suggest> suggestions = new ArrayList<SuggesterTestHelper.Suggest>();
		suggestions.add(new Suggest("rayleigh-ritz", 22));
		suggestions.add(new Suggest("rayleigh-taylor", 301));
		suggestions.add(new Suggest("pyrosim", 18));
		suggestions.add(new Suggest("rc ii", 6));
		suggestions.add(new Suggest("pyrrhocorax", 5));
		suggestions.add(new Suggest("selective reduction", 83));
		suggestions.add(new Suggest("rate of diffusion", 92));
		suggestions.add(new Suggest("pshikhopov", 9));
		suggestions.add(new Suggest("pshycosis", 6));
		suggestions.add(new Suggest("pilowsky", 12));
		suggestions.add(new Suggest("citation percentile", 7));
		suggestions.add(new Suggest("electric field polymer", 5));
		suggestions.add(new Suggest("electric filed", 39));
		suggestions.add(new Suggest("copper nanocube", 31));
		suggestions.add(new Suggest("circulating fluidized bed combustion", 8));
		suggestions.add(new Suggest("citation profile", 10));
		suggestions.add(new Suggest("citation reports", 15));
		suggestions.add(new Suggest("co-isolation", 7));
		suggestions.add(new Suggest("2000", 7));
		suggestions.add(new Suggest("free tissue transfer", 12));
		suggestions.add(new Suggest("citco", 5));
		suggestions.add(new Suggest("coprecipitation phosphor", 9));
		suggestions
				.add(new Suggest(
						"free vibration analysis of stepped beams by using adomian decomposition method",
						5));
		suggestions.add(new Suggest("coprology", 19));
		suggestions
				.add(new Suggest(
						"electrical dependencies of optical modulation capabilities in digitally addressed parallel aligned liquid crystal on silicon devices",
						6));
		suggestions.add(new Suggest("circulating tumor cell clusters", 7));
		suggestions.add(new Suggest("coprophanaeus", 11));
		suggestions.add(new Suggest("coproporphyrinogen oxidase", 8));
		suggestions.add(new Suggest("coprosma robusta", 5));
		suggestions.add(new Suggest("electrical discharge coating", 14));
		suggestions.add(new Suggest("circulating tumor cells review", 8));
		suggestions.add(new Suggest("coptis trifolia", 8));
		suggestions.add(new Suggest("co-tunneling", 6));
		suggestions.add(new Suggest("co-twin", 17));
		suggestions.add(new Suggest("coral and temperature", 11));
		suggestions.add(new Suggest("freedom food", 7));
		suggestions.add(new Suggest("freeplay", 9));
		suggestions.add(new Suggest("cityzen", 5));
		suggestions.add(new Suggest(
				"freez or freeze-dry or freeze dry or lyophili or dry", 6));
		suggestions.add(new Suggest("citrobacter farmeri", 6));
		suggestions.add(new Suggest("101", 6));
		suggestions.add(new Suggest("coral reef community", 8));
		suggestions.add(new Suggest("coral reef crisis", 12));
		suggestions.add(new Suggest("civic society", 18));
		suggestions.add(new Suggest("coral reef ecosystem service", 14));
		suggestions.add(new Suggest("androgens", 258));
		suggestions.add(new Suggest("aguda", 22));
		suggestions.add(new Suggest("aguilar-manjarrez", 5));
		suggestions.add(new Suggest("aguirre-guzman", 11));
		suggestions.add(new Suggest("anelli", 13));
		suggestions.add(new Suggest("vuyst", 6));
		suggestions.add(new Suggest("vw12o40", 5));
		suggestions.add(new Suggest("vwd", 25));
		suggestions.add(new Suggest("fuck", 100));
		suggestions.add(new Suggest("anal", 100));
		suggestions.add(new Suggest("bitch", 100));
		suggestions.add(new Suggest("arse", 100));
		suggestions.add(new Suggest("5", 100));
		suggestions.add(new Suggest("ass", 100));
		suggestions.add(new Suggest("ass-fucker", 100));
		suggestions.add(new Suggest("asses", 100));
		suggestions.add(new Suggest("assfucker", 100));
		suggestions.add(new Suggest("assfukka", 100));
		suggestions.add(new Suggest("asshole", 100));
		suggestions.add(new Suggest("assholes", 100));
		suggestions.add(new Suggest("asswhole", 100));
		suggestions.add(new Suggest("anesthetic raccoon", 100));
		suggestions.add(new Suggest("anesthetic preconditioning", 200));
				suggestions.add(new Suggest("anesthetic steroid", 986));
						suggestions.add(new Suggest("anesthetic local", 400));
		suggestions.add(new Suggest("aids death politics", 6));
		suggestions.add(new Suggest("a_s_s", 100));
		suggestions.add(new Suggest("b!tch", 100));
		suggestions.add(new Suggest("b00bs", 100));
		suggestions.add(new Suggest("b17ch", 100));
		suggestions.add(new Suggest("b1tch", 100));
		suggestions.add(new Suggest("ballbag", 100));
		suggestions.add(new Suggest("wal-mart", 152));
		suggestions.add(new Suggest("ballsack", 100));
		suggestions.add(new Suggest("wachau", 5));
		suggestions.add(new Suggest("bastard", 100));
		suggestions.add(new Suggest("beastial", 100));
		suggestions.add(new Suggest("beastiality", 100));
		suggestions.add(new Suggest("bellend", 100));
		suggestions.add(new Suggest("bestial", 100));
		suggestions.add(new Suggest("bestiality", 100));
		suggestions.add(new Suggest("bi+ch", 100));
		suggestions.add(new Suggest("biatch", 100));
		return suggestions;
	}

	private class Suggest implements Serializable {

		private String term;

		private int weight;

		public Suggest(String term, int weight) {
			this.term = term;
			this.weight = weight;

		}

		public String getTerm() {
			return term;
		}

		public void setTerm(String term) {
			this.term = term;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

	}

	private class SuggesterIterator implements InputIterator {
		private Iterator<Suggest> suggesterIterator;
		private Suggest currentSuggest;

		public SuggesterIterator(Iterator<Suggest> productIterator) {
			this.suggesterIterator = productIterator;
		}

		public boolean hasContexts() {
			return false;
		}

		public boolean hasPayloads() {
			return false;
		}

		public BytesRef next() {
			if (suggesterIterator.hasNext()) {
				currentSuggest = suggesterIterator.next();

				try {
					return new BytesRef(currentSuggest.getTerm().getBytes(
							"UTF8"));
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Couldn't convert to UTF-8", e);
				}
			} else {
				return null;
			}
		}

		public BytesRef payload() {
			return null;
		}

		public long weight() {
			return currentSuggest.getWeight();
		}

		@Override
		public Set<BytesRef> contexts() {
			// TODO Auto-generated method stub
			return null;
		}

	 

	}

}
