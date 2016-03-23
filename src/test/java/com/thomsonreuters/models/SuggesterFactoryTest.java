package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterFactory;

public class SuggesterFactoryTest {

	@Test
	public void testSuggesterFactory() {
		SuggesterFactory factory = new SuggesterFactory();
		DictionaryLoader<Lookup> dictionaryLoader = null;
		try {
			dictionaryLoader = factory.createSuggesters("S3");
		} catch (IOException e) {

		}

		assertNotNull(dictionaryLoader);
		assertEquals(
				"com.thomsonreuters.models.services.suggesters.SuggesterFromS3Bucket",
				dictionaryLoader.getClass().getCanonicalName());

		try {
			dictionaryLoader = factory.createSuggesters("S3IAM");
		} catch (IOException e) {

		}

		assertNotNull(dictionaryLoader);
		assertEquals(
				"com.thomsonreuters.models.services.suggesters.S3BucketFromS3IAMRole",
				dictionaryLoader.getClass().getCanonicalName());
	}

}
