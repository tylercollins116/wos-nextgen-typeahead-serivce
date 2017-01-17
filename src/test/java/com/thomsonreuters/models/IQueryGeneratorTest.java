package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test; 
import com.thomsonreuters.models.services.util.Property;

public class IQueryGeneratorTest {
	
	 

	@Test
	public void testArticleESEntry() {

//		String query = "blood";
//		int n = 5;
//
//		HashMap<String, String> aliasField = new HashMap<String, String>(1);
//		aliasField.put("fullrecord.summary.title", "title");
//
//		String returnVaule[] = new String[] { "fullrecord.summary.title",
//				"cuid", "fuid" };
//
//		IQueryGenerator entry = new ArticleESEntry(returnVaule, query, 0, n,
//				Property.article, aliasField);
//
//		assertEquals(
//				"{\"from\":0,\"size\":5,\"sort\": [{\"citingsrcscount\": { \"order\": \"desc\" } }],\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{\"title\":{\"query\":\"blood\",\"analyzer\":\"en_std_syn\",\"slop\":3,\"max_expansions\":50}}}}},\"fields\":[\"fullrecord.summary.title\",\"cuid\",\"fuid\"]}",
//				entry.createQuery());

	 
		 
	}

}
