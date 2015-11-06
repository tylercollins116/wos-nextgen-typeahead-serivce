package com.thomsonreuters.models.services.suggesters;

import java.io.IOException;

import org.apache.lucene.search.suggest.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;

public class S3BucketFromS3IAMRole extends SuggesterHelper implements
		DictionaryLoader<Lookup> {

	private static final Logger log = LoggerFactory
			.getLogger(S3BucketFromS3IAMRole.class);

	 
	public S3BucketFromS3IAMRole() throws IOException {

		// suggesterList.put("default", createDefaultAnalyzingSuggester());

		setS3Client(getAmazonS3());

		initializeSuggesterList();

		System.gc();
		System.gc();
	}

	@Override
	public Blockable<String, Lookup> getSuggesterList() {

		return suggesterList;
	}

	public AmazonS3 getAmazonS3() {

		return new AmazonS3Client(new InstanceProfileCredentialsProvider());
	}

}
