package com.thomsonreuters.models;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.models.ArticleEntry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

public class PrepareDictionaryTest {

	@Test
	public void testDictionary() {
		Map<String, String> map = PrepareDictionary
				.processJson("{\"keyword\":\"underage\",\"count\":20,\"id\":\"WOS:000283688100012\",\"title\":\"Why Might Adverse Childhood Experiences Lead to Underage Drinking Among US Youth? Findings From an Emergency Department-Based Qualitative Pilot Study\"}");

		Set<String> keys = map.keySet();

		for (String key : keys) {
			System.out.println(key + "\t\t" + map.get(key));
		}

		ArticleEntry article = new ArticleEntry(map);
		Assert.assertNotNull(article.getJson());
	}

}
