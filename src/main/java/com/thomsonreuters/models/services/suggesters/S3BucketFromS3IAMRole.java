package com.thomsonreuters.models.services.suggesters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.suggesterOperation.ext.AnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.models.ArticleEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.CategoryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

public class S3BucketFromS3IAMRole extends SuggesterHelper implements
		DictionaryLoader<Lookup> {

	private static final Logger log = LoggerFactory
			.getLogger(S3BucketFromS3IAMRole.class);

	private final Blockable<String, Lookup> suggesterList = new BlockingHashTable<String, Lookup>();

	public S3BucketFromS3IAMRole() throws IOException {

		// suggesterList.put("default", createDefaultAnalyzingSuggester());

		initializeSuggesterList();

		System.gc();
		System.gc();
	}

	@Override
	public Blockable<String, Lookup> getSuggesterList() {

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
				log.info("path to bucket : " + bucketName);

			} else if (property.isDictionaryPathRelated()) {
				log.info("**************************************************************");
				log.info("path to dictionary : " + property.toString());
				log.info("**************************************************************");
				dictionaryProperties.add(property.toString());
			}
		}

		if (bucketName != null && bucketName.trim().length() > 0) {
			for (String dictionaryProperty : dictionaryProperties) {
				Property property = PropertyValue
						.getProperty(dictionaryProperty);
				String value = ConfigurationManager.getConfigInstance()
						.getString(property.toString());
				
				
				getStoredPathInfo();

				if (property.isDictionaryPathRelated()
						&& isDictionaryAlreadyLoaded(
								property.getDictionayName(), value)) {
 
					log.info("**************************************************************");
					log.info("Trying to Load the  dictionary for "
							+ dictionaryProperty + " BucketName : "
							+ bucketName + "  ,Path : " + value
							+ " again  .. reloading ignored ");
					log.info("**************************************************************");

					continue;
 
				}

				log.info("**************************************************************");
				log.info(" Loading dictionary for " + dictionaryProperty
						+ " BucketName : " + bucketName + "  ,Path : " + value);
				log.info("**************************************************************");
				try {

					S3Object s3file = s3Client.getObject(bucketName, value);
					log.info("**************************************************************");
					log.info("Successfully got access to S3 bucket : "
							+ bucketName);
					log.info("**************************************************************");

					InputStream is = s3file.getObjectContent();

					/********** Important code to work on ************************/

					if (property.getDictionayName().equalsIgnoreCase(
							"organization")) {

						AnalyzingSuggesterExt suggester = createAnalyzingSuggesterForOrganization(is);
						suggesterList.put(property.getDictionayName(),
								suggester);

					} else if (property.getDictionayName().equalsIgnoreCase(
							"article")) {

						AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, ArticleEntry.class);
						suggesterList.put(property.getDictionayName(),
								suggester);
					} else if (property.getDictionayName().equalsIgnoreCase(
							"wos")) {

						AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, KeywordEntry.class);
						suggesterList.put(property.getDictionayName(),
								suggester);
					} else if (property.getDictionayName().equalsIgnoreCase(
							"categories")) {
						AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, CategoryEntry.class);
						suggesterList.put(property.getDictionayName(),
								suggester);

					}

					/***************************** End **********************************/

					storeLoadedDictoanryInfo(property.getDictionayName(), value);

					log.info("Loading dictionary for " + dictionaryProperty
							+ " completed successfully.");
				} catch (Exception e) {

					log.info(" fail loading dictionary for "
							+ dictionaryProperty);

					e.printStackTrace();
				}
			}
		}

	}

	public void reloadDictionary(String propertyName) throws IOException {
		log.info("**************************************************************");
		log.info("reloading dictionary of " + propertyName + " starting");
		log.info("**************************************************************");
		Property bucketProperty = PropertyValue.getProperty(Property.S3_BUCKET);
		String bucketName = ConfigurationManager.getConfigInstance().getString(
				bucketProperty.toString());

		Property property = PropertyValue.getProperty(propertyName);
		String dictionaryPath = ConfigurationManager.getConfigInstance()
				.getString(property.toString());
		
		
		getStoredPathInfo();

		if (property.isDictionaryPathRelated()
				&& isDictionaryAlreadyLoaded(property.getDictionayName(),
						dictionaryPath)) {
			log.info("**************************************************************");
			log.info("Try to reLoad the  dictionary for " + propertyName
					+ " BucketName : " + bucketName + "  ,Path : "
					+ dictionaryPath + " again  .. reloading ignored ");
 
			log.info("**************************************************************");

			return;
 
		}

		AmazonS3 s3Client = getAmazonS3();

		S3Object s3file = s3Client.getObject(bucketName, dictionaryPath);
		InputStream is = s3file.getObjectContent();

		/********** Important code to work on ************************/

		if (property.getDictionayName().equalsIgnoreCase("organization")) {

			AnalyzingSuggesterExt suggester = createAnalyzingSuggesterForOrganization(is);
			suggesterList.put(property.getDictionayName(), suggester);

		} else if (property.getDictionayName().equalsIgnoreCase("article")) {

			AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, ArticleEntry.class);
			suggesterList.put(property.getDictionayName(), suggester);
		} else if (property.getDictionayName().equalsIgnoreCase("wos")) {

			AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, KeywordEntry.class);
			suggesterList.put(property.getDictionayName(), suggester);
		} else if (property.getDictionayName().equalsIgnoreCase("categories")) {
			AnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, CategoryEntry.class);
			suggesterList.put(property.getDictionayName(), suggester);

		}

		/***************************** End **********************************/

		storeLoadedDictoanryInfo(property.getDictionayName(), dictionaryPath);

		log.info("**************************************************************");
		log.info("reloading dictionary of " + propertyName + " completed");
		log.info("**************************************************************");
	}

	public AmazonS3 getAmazonS3() {

		return new AmazonS3Client(new InstanceProfileCredentialsProvider());
	}

}
