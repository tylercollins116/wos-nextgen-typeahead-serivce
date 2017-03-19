package com.thomsonreuters.models;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.thomsonreuters.models.services.suggesterOperation.models.company.CompanyTypeaheadSuggester;

public class CompanyTypeaheadVariationTest {
	
	@Test
	public void testCompanyTypeahead() throws Exception {

		Long totalMemory = Runtime.getRuntime().totalMemory();

		CompanyTypeaheadSuggester suggester = new CompanyTypeaheadSuggester(
				ClassLoader.class
						.getResourceAsStream("/sampledict/company-dict-test1.dict"));

	 
		String result = (suggester.lookup("Solaris", 10, 2, true, false));
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(result);
		String prettyJsonString = gson.toJson(je);
		
		System.out.println(prettyJsonString);
	}

}
