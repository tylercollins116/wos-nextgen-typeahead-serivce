package com.thomsonreuters.models.services.suggesters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

public class SuggesterFromS3Bucket extends SuggesterHelper implements
		DictionaryLoader<AnalyzingSuggester> {

	private final Blockable<String, AnalyzingSuggester> suggesterList = new BlockingHashTable<String, AnalyzingSuggester>();

	public SuggesterFromS3Bucket() throws IOException {	
		
		suggesterList.put("default", createDefaultAnalyzingSuggester());
		
		initializeSuggesterList();
	}

	@Override
	public Blockable<String, AnalyzingSuggester> getSuggesterList() {

		return suggesterList;
	}

	public void initializeSuggesterList() throws IOException {

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		AmazonS3Client s3Client = new AmazonS3Client(getCredentials());

		String bucketName = null;
		List<String> dictionaryProperties = new ArrayList<String>();

		while (keys.hasNext()) {
			String key = keys.next();

			Property property = PropertyValue.getProperty(key);

			if (property.isBucketName()) {
				bucketName = ConfigurationManager.getConfigInstance()
						.getString(key);
			} else if (property.isDictionaryPathRelated()) {
				dictionaryProperties.add(property.toString());
			}
		}

		if (bucketName != null && bucketName.trim().length() > 0) {
			for (String dictionaryProperty : dictionaryProperties) {
				Property property = PropertyValue
						.getProperty(dictionaryProperty);
				String value = ConfigurationManager.getConfigInstance()
						.getString(property.toString());

				S3Object s3file = s3Client.getObject(bucketName, value);
				InputStream is = s3file.getObjectContent();
				
				AnalyzingSuggester suggester=createAnalyzingSuggester(is);
				 
				suggesterList.put(property.getDictionayName(),suggester);
			}
		}

	}

	public AWSCredentials getCredentials() {

		String awsKey = ConfigurationManager.getConfigInstance().getString(
				Property.AWS_KEY);

		String awsKeyId = ConfigurationManager.getConfigInstance().getString(
				Property.AWS_KEY_ID);
		 
		AWSCredentials credential = new AWSCredentialsImpl(awsKeyId, awsKey);

		return credential;

	}

	public void reloadDictionary(String propertyName) throws IOException {

		Property bucketProperty = PropertyValue.getProperty(Property.S3_BUCKET);
		String bucketName = ConfigurationManager.getConfigInstance().getString(
				bucketProperty.toString());

		Property property = PropertyValue.getProperty(propertyName);
		String dictionaryPath = ConfigurationManager.getConfigInstance()
				.getString(property.toString());

		AmazonS3Client s3Client = new AmazonS3Client(getCredentials());

		S3Object s3file = s3Client.getObject(bucketName, dictionaryPath);
		InputStream is = s3file.getObjectContent();

		AnalyzingSuggester suggester = createAnalyzingSuggester(is);

		 suggesterList.put(property.getDictionayName(), suggester);
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
