package com.thomsonreuters.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;



public class SuggestorTest {
	
	
	@Test
	public void sanityCheck() {


		Map<String,String> wordsToCheck=new HashMap<String, String>();
		wordsToCheck.put("random fore", "random forest");
		wordsToCheck.put("ashm", "ashma");
		wordsToCheck.put("camara", "camera");
		wordsToCheck.put("2000	", "2000");

		
		for (Map.Entry<String, String> entry : wordsToCheck.entrySet()) {
			String word=entry.getKey();
			String value=entry.getValue();
			List<SuggestData> result=Suggester.lookup("wos",word, 1);
			if (result!=null && result.size()>=1) {
				Assert.assertFalse("Failed on word "+word+". Lookup("+word+")="+value,word.equals(value));
			}
			else Assert.fail("Fail to find suggestion for "+word);
		}

		
	}
	
	
	@Test
	public void checkProfanityFilter() {
		List<String> badWords = Arrays.asList(
				"fuck","anal","bitch"
				);

		for (String badword : badWords) {
			List<SuggestData> result=Suggester.lookup("wos",badword, 1);
			if (result!=null && result.size()>=1) {
				Assert.assertFalse("Profanity filter failed on word "+badword,badword.equals(result.get(0).value));
			}
			else Assert.fail("Fail to find suggestion for "+badword);
			
		}
	}
	
	
}
