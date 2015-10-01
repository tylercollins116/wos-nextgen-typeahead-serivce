package com.thomsonreuters.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class SuggestorTest {

	private SuggesterTestHelper suggesterHelper = null;

	@Before
	public void setup() {
		suggesterHelper = new SuggesterTestHelper();
	}

	@After
	public void destroy() {
		suggesterHelper = null;
	}

	@Test
	public void nullCheck() {
		
		assertNotNull(suggesterHelper);
		 
	}

	@Test
	public void sanityCheck() {

		Map<String, String> wordsToCheck = new HashMap<String, String>();
		wordsToCheck.put("anesthetic rac", "anesthetic rac");
		wordsToCheck.put("ane", "ane");
		wordsToCheck.put("vw12", "vw12");
		wordsToCheck.put("2000	", "2000");
		wordsToCheck.put("5", "2000");

		for (Map.Entry<String, String> entry : wordsToCheck.entrySet()) {

			String word = entry.getKey();
			String value = entry.getValue();
			List<LookupResult> result = new ArrayList<LookupResult>();
			try {
				result = suggesterHelper.getAnalyzingSuggester().lookup(word,
						false, 1);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (result != null && result.size() >= 1) {
				/** must find all the words in Dictionary **/
			} else
				Assert.fail("Fail to find suggestion for " + word);

		}

	}

	@Test
	public void checkProfanityFilter() {

		List<String> badWords = Arrays.asList("fuck", "anal", "bitch");
		List<LookupResult> result = new ArrayList<LookupResult>();

		for (String badword : badWords) {

			try {
				result = suggesterHelper.getAnalyzingSuggester().lookup(
						badword, false, 10);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (result != null && result.size() >= 1) {

				for (LookupResult badword_ : result) {

					/**
					 * must no find any words in the dictionary but can find
					 * word start with those words with different meanings like
					 * for "anal" can find "anelli"
					 **/

					if (badWords.equals(badword_.key.toString()))
						Assert.fail("should not find suggestion for Profanity word like "
								+ badword);
				}
			}

		}
	}

	@Test
	public void weightCheck() {

		String word = "anestheti";

		List<LookupResult> result = new ArrayList<LookupResult>();
		try {
			result = new SuggesterTestHelper().getAnalyzingSuggester().lookup(
					word, false, 10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null && result.size() >= 1) {
			/** must find all the words in Dictionary **/

			if (!result.get(0).key.toString().equals("anesthetic steroid")) {
				Assert.fail("must be \"anesthetic steroid\" first according to the weight ");
			}

		} else
			Assert.fail("Fail to find suggestion for " + word);

	}

}
