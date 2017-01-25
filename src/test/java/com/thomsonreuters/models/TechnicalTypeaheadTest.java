package com.thomsonreuters.models;

import java.util.Date;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.suggesterOperation.models.company.TechnicalTypeaheadSuggester;

public class TechnicalTypeaheadTest {

	@Test
	public void testCompanyTypeahead() throws Exception {

		Long totalMemory = Runtime.getRuntime().totalMemory();
		
	
		
		long start = System.currentTimeMillis();
		
		System.out.println(new Date());

		final TechnicalTypeaheadSuggester suggester = new TechnicalTypeaheadSuggester(
				ClassLoader.class
						.getResourceAsStream("/sampledict/technical.dict"));
		
		
		System.out.println(new Date());
		
		System.out.println(System.currentTimeMillis()-start);


		
		SuggestData result = (suggester.lookup("INNAT", 2, 3,
				false));

		List<Suggestions> allSuggestions = result.suggestions;
		
		assertEquals("INNATE AND ADAPTIVE IMMUNE RESPONSES", result.suggestions.get(0).info.get(0).value);
		
		
		 

		 
	}

	 

}
