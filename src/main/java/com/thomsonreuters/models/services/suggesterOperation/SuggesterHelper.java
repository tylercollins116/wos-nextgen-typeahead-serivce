package com.thomsonreuters.models.services.suggesterOperation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.models.CategoryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.CompanyEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.EntryIterator;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.TopicEntry;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

public abstract class SuggesterHelper {

	private AmazonS3 s3Client = null;

	protected final Blockable<String, Lookup> suggesterList = new BlockingHashTable<String, Lookup>();

	public void setS3Client(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	public static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);

	private static final Map<String, String> dictionaryPaths = new HashMap<String, String>();

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterHelper.class);

	static {
		try {
			stopSet.addAll(WordlistLoader.getWordSet(IOUtils.getDecodingReader(
					ClassLoader.class
							.getResourceAsStream("/data/profanityWords.txt"),
					StandardCharsets.UTF_8)));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);

	private Analyzer queryAnalyzer = new StandardAnalyzer(
			CharArraySet.EMPTY_SET);

	public synchronized final boolean isDictionaryAlreadyLoaded(String path,
			String dictionary) {

		String dictionaryInfo = null;

		log.info("Checking wheather dictionary with following path " + path
				+ " " + dictionary + "  already loaded ");

		if ((dictionaryInfo = dictionaryPaths.get(path.toLowerCase().trim())) != null
				&& dictionaryInfo.trim().equalsIgnoreCase(dictionary.trim())) {
			return true;
		}

		log.info("Checking passed .. safe to load " + path + " " + dictionary);

		return false;
	}

	public synchronized final boolean storeLoadedDictoanryInfo(String path,
			String dictionary) {

		dictionaryPaths.put(path.toLowerCase().trim(), dictionary.toLowerCase()
				.trim());
		log.info("Stored " + path.toLowerCase().trim() + "\t\t"
				+ dictionary.toLowerCase().trim());
		return true;
	}

	public synchronized final void getStoredPathInfo() {

		log.info("***************************************************************************");
		Set<String> keys = dictionaryPaths.keySet();

		for (String key : keys) {
			log.info("Founds following dictionary already loaded \"" + key
					+ "\" from path " + dictionaryPaths.get(key));
		}

		log.info("***************************************************************************");

	}

	public static void loadFuzzynessThreshold(String triggredProperty) {

		String fuzzynessThreshold = null;
		if (triggredProperty != null
				&& (fuzzynessThreshold = triggredProperty.trim())
						.equalsIgnoreCase(PropertyValue.FUZZYNESS_THRESHOLD_PATH)) {

			fuzzynessThreshold = ConfigurationManager.getConfigInstance()
					.getString(fuzzynessThreshold);

			try {

				PropertyValue.FUZZTNESS_THRESHOLD = Integer
						.parseInt(fuzzynessThreshold);

			} catch (NumberFormatException nfe) {
			}
		}
	}

	public void initializeSuggesterList() throws IOException {

		if (s3Client == null) {
			throw new IOException(
					" S3Client found null pls initilize s3Client first");
		}

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

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
			} else {

				loadFuzzynessThreshold(key);
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
							Property.organization)) {

						TRAnalyzingSuggesterExt suggester = createAnalyzingSuggesterForOrganization(is);
						suggesterList.put(property.getDictionayName(),
								suggester);

					} else if (property.getDictionayName().equalsIgnoreCase(
							Property.companyterms)) {

						TRAnalyzingSuggesterExt suggester = createAnalyzingSuggesterForCompany(is);
						suggesterList.put(property.getDictionayName(),
								suggester);

					} else if (property.getDictionayName().equalsIgnoreCase(
							Property.wos)) {

						TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, new KeywordEntry());

						suggesterList.put(property.getDictionayName(),
								suggester);
					} else if (property.getDictionayName().equalsIgnoreCase(
							Property.topic)) {

						TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, new TopicEntry());

						suggesterList.put(property.getDictionayName(),
								suggester);
					} else if (property.getDictionayName().equalsIgnoreCase(
							Property.category)) {
						TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
								is, new CategoryEntry());
						suggesterList.put(property.getDictionayName(),
								suggester);

					}

					/***************************** End **********************************/

					storeLoadedDictoanryInfo(property.getDictionayName(), value);
					log.info("**************************************************************");

					log.info("  Loading dictionary for " + dictionaryProperty
							+ " completed successfully.");
					log.info("**************************************************************");

				} catch (Exception e) {

					log.info(" fail loading dictionary for "
							+ dictionaryProperty);

					e.printStackTrace();
				}
			}
		}

	}

	public void reloadDictionary(String propertyName) throws IOException {

		if (s3Client == null) {
			throw new IOException(
					" S3Client found null pls initilize s3Client first");
		}

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

		S3Object s3file = s3Client.getObject(bucketName, dictionaryPath);
		InputStream is = s3file.getObjectContent();

		/********** Important code to work on ************************/

		if (property.getDictionayName().equalsIgnoreCase(Property.organization)) {

			TRAnalyzingSuggesterExt suggester = createAnalyzingSuggesterForOrganization(is);
			suggesterList.put(property.getDictionayName(), suggester);

		} else if (property.getDictionayName().equalsIgnoreCase(
				Property.companyterms)) {

			TRAnalyzingSuggesterExt suggester = createAnalyzingSuggesterForCompany(is);
			suggesterList.put(property.getDictionayName(), suggester);

		} else if (property.getDictionayName().equalsIgnoreCase(Property.wos)) {

			com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, new KeywordEntry());
			suggesterList.put(property.getDictionayName(), suggester);
		} else if (property.getDictionayName().equalsIgnoreCase(
				Property.category)) {
			TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, new CategoryEntry());
			suggesterList.put(property.getDictionayName(), suggester);

		}

		else if (property.getDictionayName().equalsIgnoreCase(Property.topic)) {

			TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
					is, new TopicEntry());

			suggesterList.put(property.getDictionayName(), suggester);
		}

		/***************************** End **********************************/

		storeLoadedDictoanryInfo(property.getDictionayName(), dictionaryPath);

		log.info("**************************************************************");
		log.info(" reloading dictionary of " + propertyName + " completed");
		log.info("**************************************************************");
	}

	/****************************************************************************************/
	/********************* Creating Analyzers starts Here ************************************/
	/****************************************************************************************/

	public TRAnalyzingSuggester createAnalyzingSuggester(InputStream is)
			throws IOException {

		FileDictionary dictionary = (new FileDictionary(
				new BufferedInputStream(is)));

		TRAnalyzingSuggester suggester = new TRFuzzySuggester(indexAnalyzer,
				queryAnalyzer);

		suggester.build(dictionary);

		try {
			is.close();
		} catch (Exception e) {
		}

		return suggester;
	}

	public TRAnalyzingSuggesterExt createAnalyzingSuggesterForOrganization(
			InputStream is) {
		TRAnalyzingSuggesterExt suggester = null;
		try {

			PrepareDictionary dictionary = new PrepareDictionary(is,
					new OrganizationEntry());

			suggester = new TRFuzzySuggesterExt(indexAnalyzer, queryAnalyzer);

			suggester.build(new EntryIterator(dictionary));

			dictionary.close();
			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	public TRAnalyzingSuggester createAnalyzingSuggesterForOthers(
			InputStream is, Entry enteryClass) {
		TRAnalyzingSuggester suggester = null;
		try {

			PrepareDictionary dictionary = new PrepareDictionary(is,
					enteryClass);

			if (enteryClass instanceof KeywordEntry) {
				suggester = new TRFuzzySuggester(indexAnalyzer, queryAnalyzer,
						0);
			} else {
				suggester = new TRFuzzySuggester(indexAnalyzer, queryAnalyzer);
			}

			suggester.build(new EntryIterator(dictionary));

			dictionary.close();
			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	public TRAnalyzingSuggesterExt createAnalyzingSuggesterForCompany(
			InputStream is) {
		TRAnalyzingSuggesterExt suggester = null;
		try {

			PrepareDictionary dictionary = new PrepareDictionary(is,
					new CompanyEntry());

			suggester = new TRFuzzySuggesterExt(indexAnalyzer, queryAnalyzer);

			suggester.build(new EntryIterator(dictionary));

			dictionary.close();
			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

}
