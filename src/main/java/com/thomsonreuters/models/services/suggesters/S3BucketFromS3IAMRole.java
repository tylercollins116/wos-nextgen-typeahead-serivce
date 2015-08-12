package com.thomsonreuters.models.services.suggesters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.handler.HealthCheck;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

public class S3BucketFromS3IAMRole extends SuggesterHelper implements
		DictionaryLoader<AnalyzingSuggester> {
	
	private static final Logger log = LoggerFactory.getLogger(S3BucketFromS3IAMRole.class);

	private final Blockable<String, AnalyzingSuggester> suggesterList = new BlockingHashTable<String, AnalyzingSuggester>();

	public S3BucketFromS3IAMRole() throws IOException {

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

		AmazonS3 s3Client = getAmazonS3();

		String bucketName = null;
		List<String> dictionaryProperties = new ArrayList<String>();

		while (keys.hasNext()) {
			String key = keys.next();

			Property property = PropertyValue.getProperty(key);

			if (property.isBucketName()) {
				bucketName = ConfigurationManager.getConfigInstance()
						.getString(key);
				log.info("path to bucket : "+bucketName);
				
			} else if (property.isDictionaryPathRelated()) {				
				log.info("path to dictionary : "+property.toString());
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
				
				log.info("Successfully got access to S3 bucket : "+bucketName);
				
				InputStream is = s3file.getObjectContent();

				AnalyzingSuggester suggester = createAnalyzingSuggester(is);

				suggesterList.put(property.getDictionayName(), suggester);
			}
		}

	}

	public void reloadDictionary(String propertyName) throws IOException {

		Property bucketProperty = PropertyValue.getProperty(Property.S3_BUCKET);
		String bucketName = ConfigurationManager.getConfigInstance().getString(
				bucketProperty.toString());

		Property property = PropertyValue.getProperty(propertyName);
		String dictionaryPath = ConfigurationManager.getConfigInstance()
				.getString(property.toString());

		AmazonS3 s3Client = getAmazonS3();

		S3Object s3file = s3Client.getObject(bucketName, dictionaryPath);
		InputStream is = s3file.getObjectContent();

		AnalyzingSuggester suggester = createAnalyzingSuggester(is);

		suggesterList.put(property.getDictionayName(), suggester);
	}

	public AmazonS3 getAmazonS3() {

		return new AmazonS3Client(new InstanceProfileCredentialsProvider());
	}

}
