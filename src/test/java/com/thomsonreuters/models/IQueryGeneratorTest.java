package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import com.thomsonreuters.models.services.ESoperation.ArticleESEntry;
import com.thomsonreuters.models.services.ESoperation.IQueryGenerator;
import com.thomsonreuters.models.services.ESoperation.PatentESEntry;
import com.thomsonreuters.models.services.ESoperation.PeopleESEntry;
import com.thomsonreuters.models.services.ESoperation.PostESEntry;
import com.thomsonreuters.models.services.util.Property;

public class IQueryGeneratorTest {

	@Test
	public void testArticleESEntry() {

		String query = "blood";
		int n = 5;

		HashMap<String, String> aliasField = new HashMap<String, String>(1);
		aliasField.put("fullrecord.summary.title", "title");

		String returnVaule[] = new String[] { "fullrecord.summary.title",
				"cuid", "fuid" };

		IQueryGenerator entry = new ArticleESEntry(returnVaule, query, 0, n,
				Property.article, aliasField);

		assertEquals(
				"{\"from\":0,\"size\":5,\"sort\": [{\"citingsrcscount\": { \"order\": \"desc\" } }],\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{\"title\":{\"query\":\"blood\",\"analyzer\":\"en_std_syn\",\"slop\":3,\"max_expansions\":50}}}}},\"fields\":[\"fullrecord.summary.title\",\"cuid\",\"fuid\"]}",
				entry.createQuery());

		/** ----------------------------------------------- **/

		returnVaule = new String[] { "fullrecord.summary.country",
				"institution", "role", "fullrecord.summary.authors",
				"fullrecord.summary.uid" };

		aliasField = new HashMap<String, String>(3);
		aliasField.put("fullrecord.summary.country", "country");
		aliasField.put("fullrecord.summary.authors", "name");
		aliasField.put("fullrecord.summary.uid", "id");

		entry = new PeopleESEntry(returnVaule, query, 0, n, Property.people,
				aliasField);
 
		assertEquals(
				"{\"from\":0,\"size\":5,\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{\"authors\":{\"query\":\"blood\",\"slop\":3,\"max_expansions\":50}}}}},\"fields\":[\"fullrecord.summary.country\",\"institution\",\"role\",\"fullrecord.summary.authors\",\"fullrecord.summary.uid\"]}",
				entry.createQuery());

		/** ----------------------------------------------- **/

		returnVaule = new String[] { "fullrecord.summary.uid",
				"fullrecord.summary.title",
				"fullrecord.summary.citingsrcscount" };

		aliasField = new HashMap<String, String>(3);
		aliasField.put("fullrecord.summary.title", "title");
		aliasField.put("fullrecord.summary.uid", "patentno");
		aliasField.put("fullrecord.summary.citingsrcscount", "timeCited");

		entry = new PatentESEntry(returnVaule, query, 0, n, Property.patent,
				aliasField);
		
		assertEquals("{\"from\":0,\"size\":5,\"sort\": [{\"citingsrcscount\": { \"order\": \"desc\" } }],\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{\"title\":{\"query\":\"blood\",\"analyzer\":\"en_std_syn\",\"slop\":3,\"max_expansions\":50}}}}},\"fields\":[\"fullrecord.summary.uid\",\"fullrecord.summary.title\",\"fullrecord.summary.citingsrcscount\"]}",entry.createQuery());

		
		/** ----------------------------------------------- **/

		
		  returnVaule = new String[] {
				"fullrecord.summary.uid",
				"fullrecord.summary.title",
				"fullrecord.summary.truid",
				"fullrecord.summary.pubdate" };

		 aliasField = new HashMap<String, String>(
				4);
		aliasField.put("fullrecord.summary.title", "title");
		aliasField.put("fullrecord.summary.uid", "uid");
		aliasField.put("fullrecord.summary.truid", "truid");
		aliasField.put("fullrecord.summary.pubdate",
				"publishdate");

		  entry = new PostESEntry(returnVaule,
				query, 0, n, Property.post, aliasField);
		  
		  assertEquals("{\"from\":0,\"size\":5,\"sort\": [{\"sortdate\": { \"order\": \"desc\" } }],\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{\"title\":{\"query\":\"blood\",\"slop\":3,\"max_expansions\":50}}}}},\"fields\":[\"fullrecord.summary.uid\",\"fullrecord.summary.title\",\"fullrecord.summary.truid\",\"fullrecord.summary.pubdate\"]}",entry.createQuery());
	
	}

}
