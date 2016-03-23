package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;

public class S3BucketFromS3IAMRoleTest {

	@Test
	public void BlankSuggestertest() {

		SuggesterConfiguration configuration = new SuggesterConfiguration();
		DictionaryLoader<Lookup> dictionaryReader = configuration
				.getDictionaryAnalyzer();
		assertEquals(
				"com.thomsonreuters.models.services.suggesters.S3BucketFromS3IAMRole",
				dictionaryReader.getClass().getCanonicalName());

	}

}
