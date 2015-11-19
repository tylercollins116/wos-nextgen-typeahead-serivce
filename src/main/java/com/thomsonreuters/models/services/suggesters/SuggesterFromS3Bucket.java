package com.thomsonreuters.models.services.suggesters;

import java.io.IOException;

import org.apache.lucene.search.suggest.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.Property;

public class SuggesterFromS3Bucket extends SuggesterHelper implements
		DictionaryLoader<Lookup> {

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterFromS3Bucket.class);

	public SuggesterFromS3Bucket() throws IOException {

		// suggesterList.put("default", createDefaultAnalyzingSuggester());

		setS3Client(new AmazonS3Client(getCredentials()));

		initializeSuggesterList();
		System.gc();
		System.gc();
	}

	@Override
	public Blockable<String, Lookup> getSuggesterList() {

		return suggesterList;
	}

	public AWSCredentials getCredentials() {

		String awsKey = ConfigurationManager.getConfigInstance().getString(
				Property.AWS_KEY);

		String awsKeyId = ConfigurationManager.getConfigInstance().getString(
				Property.AWS_KEY_ID);

		AWSCredentials credential = new AWSCredentialsImpl(awsKeyId, awsKey);

		return credential;

	}

	class AWSCredentialsImpl implements AWSCredentials {

		private final String AWSAccessKeyId;
		private final String AWSSecretKey;

		public AWSCredentialsImpl(String AWSAccessKeyId, String AWSSecretKey) {

			this.AWSAccessKeyId = AWSAccessKeyId;
			this.AWSSecretKey = AWSSecretKey;

		}

		@Override
		public String getAWSAccessKeyId() {
			// TODO Auto-generated method stub
			return this.AWSAccessKeyId;
		}

		@Override
		public String getAWSSecretKey() {
			// TODO Auto-generated method stub
			return this.AWSSecretKey;
		}

	}

}
