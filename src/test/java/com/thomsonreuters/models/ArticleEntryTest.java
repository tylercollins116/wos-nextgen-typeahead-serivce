package com.thomsonreuters.models;

import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.thomsonreuters.models.services.suggesterOperation.models.ArticleEntry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

public class ArticleEntryTest {

	@Test
	public void test() {

		Map<String, String> JsonToMap = PrepareDictionary
				.processJson("{\"keyword\":\"adsorption\",\"count\":11,\"UT\":\"WOS:000337985600052\",\"title\":\"Adsorption behavior and mechanism of perfluorinated compounds on various adsorbents-A review\",\"fuid\":\"472494968WOS1\"}");

		ArticleEntry entry = new ArticleEntry(JsonToMap);
		assertNotNull(entry.toString());
	}

}
