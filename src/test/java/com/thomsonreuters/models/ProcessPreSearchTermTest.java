package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

 
import com.thomsonreuters.models.services.suggesters.ProcessPreSearchTerm;

public class ProcessPreSearchTermTest {

	@Test
	public void test() {

		List<String> preSearchedTerms = new ArrayList<String>();
		
		
		preSearchedTerms.add("pyrrhocorax");
		preSearchedTerms.add("selective reduction");
		preSearchedTerms.add("rate of diffusion");
		preSearchedTerms.add("pshikhopov");
		preSearchedTerms.add("pshycosis");
		preSearchedTerms.add("pilowsky");
		preSearchedTerms.add("citation percentile");
		preSearchedTerms.add("electric field polymer");
		preSearchedTerms.add("electric filed");
		preSearchedTerms.add("copper nanocube");
		preSearchedTerms.add("blood fluidized bed combustion");
		preSearchedTerms.add("citation profile");
		preSearchedTerms.add("citation reports");
		preSearchedTerms.add("co-isolation");
		
		
		 

		ProcessPreSearchTerm object = new ProcessPreSearchTerm();

		String[] result = object.getSuggestions(
				preSearchedTerms.toArray(new String[] {}), "blo");

		for (String st : result) {
			assertEquals(st, "blood fluidized bed combustion");
		}

		result = object.getSuggestions(
				preSearchedTerms.toArray(new String[] {}), "rat");
		for (String st : result) {
			assertEquals(st, "rate of diffusion");
		}

	}

}
