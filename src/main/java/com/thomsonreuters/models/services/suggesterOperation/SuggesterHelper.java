package com.thomsonreuters.models.services.suggesterOperation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
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
import com.thomsonreuters.models.services.suggesterOperation.models.CompanyEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.DictionaryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.EntryIterator;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.company.CompanyTypeaheadSuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.company.TechnicalTypeaheadSuggester;
import com.thomsonreuters.models.services.util.Blockable;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.DictionaryInfo;
import com.thomsonreuters.models.services.util.GroupTerms;
import com.thomsonreuters.models.services.util.PrepareDictionary;
import com.thomsonreuters.models.services.util.Property;

public abstract class SuggesterHelper {

	private AmazonS3 s3Client = null;

	protected final Blockable<String, Lookup> suggesterList = new BlockingHashTable<String, Lookup>();

	public void setS3Client(AmazonS3 s3Client) {
		this.s3Client = s3Client;
	}

	public static final CharArraySet stopSet = new CharArraySet(
			CharArraySet.EMPTY_SET, false);

	private static final Map<String, DictionaryInfo> dictionaryInfos = new HashMap<String, DictionaryInfo>();

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

	public synchronized final void getStoredPathInfo() {

		log.info("***************************************************************************");
		Set<String> keys = dictionaryInfos.keySet();

		for (String key : keys) {
			log.info("Founds following dictionary already loaded \"" + key
					+ "\" from path " + dictionaryInfos.get(key));
		}

		log.info("***************************************************************************");

	}

	public void initializeSuggesterList() throws IOException {

		if (s3Client == null) {
			throw new IOException(
					" S3Client found null pls initilize s3Client first");
		}

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		String bucketName = null;

		Property property = new GroupTerms();

		List<String> allDictionaryRelatedInfos = new ArrayList<String>();

		while (keys.hasNext()) {
			String key = keys.next();

			if (property.isDictionaryRelated(key)) {
				allDictionaryRelatedInfos.add(key);
			}
		}

		property.groupTermsBasedOnDictionary(allDictionaryRelatedInfos,
				dictionaryInfos);

		loadAllDictionaryProperyValues(dictionaryInfos);

		keys = ConfigurationManager.getConfigInstance().getKeys();

		while (keys.hasNext()) {
			String key = keys.next();
			if (property.isBucketName(key)) {
				bucketName = ConfigurationManager.getConfigInstance()
						.getString(key);
				log.info("path to bucket : " + bucketName);

			}
		}

		Set<String> dictionaries = dictionaryInfos.keySet();

		for (String key : dictionaries) {

			DictionaryInfo info = dictionaryInfos.get(key);

			getStoredPathInfo();

			String s3bucket = info.getInfos().get(property.S3_BUCKET_SUFFIX);

			s3bucket = s3bucket == null ? bucketName : s3bucket;

			String s3Path = info.getDictionaryPath();

			if (s3bucket == null) {
				s3bucket = bucketName;
			}

			StartLoadingProcess(info, bucketName, false);

		}

	}

	public void reloadDictionary(String propertyName) throws IOException {

		if (s3Client == null) {
			throw new IOException(
					" S3Client found null pls initilize s3Client first");
		}

		Property property = new GroupTerms();

		if (!property.isDictionaryRelated(propertyName)) {
			return;
		}

		if (property.isBucketName(propertyName)) {
			initializeSuggesterList();
			return;
		}

		log.info("**************************************************************");
		log.info("reloading dictionary of " + propertyName + " starting");
		log.info("**************************************************************");

		String dictionaryName = property.getDictionayName(propertyName);

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		List<String> allkeyReferenctToChange = new ArrayList<String>();
		while (keys.hasNext()) {
			String key = keys.next();
			if (property.isDictionaryRelated(key)) {
				if (property.getDictionayName(key).trim()
						.equalsIgnoreCase(dictionaryName)) {

					allkeyReferenctToChange.add(key);
				}

			}
		}

		Map<String, DictionaryInfo> changeDictionaryInfos = new HashMap<String, DictionaryInfo>();

		property.groupTermsBasedOnDictionary(allkeyReferenctToChange,
				changeDictionaryInfos);

		loadAllDictionaryProperyValues(changeDictionaryInfos);

		DictionaryInfo changedDictionaryInfo = changeDictionaryInfos
				.get(dictionaryName);

		DictionaryInfo realDictionaryInfo = dictionaryInfos.get(dictionaryName);

		if (realDictionaryInfo == null
				|| (!realDictionaryInfo.compare(changedDictionaryInfo))) {
			
			if(realDictionaryInfo==null){
				log.info("Reloading new dictionary "+dictionaryName);	
			}else{
				log.info("updating  dictionary "+dictionaryName);	
			}

			String s3bucket = changedDictionaryInfo.getInfos().get(
					property.S3_BUCKET_SUFFIX);

			s3bucket = s3bucket == null ? ConfigurationManager
					.getConfigInstance().getString(Property.S3_BUCKET)
					: s3bucket;

			String s3Path = changedDictionaryInfo.getDictionaryPath();

			StartLoadingProcess(changedDictionaryInfo, s3bucket, true);
			
			
			/**
			 * new changes must replace the old one 
			 */
			dictionaryInfos.put(dictionaryName, changedDictionaryInfo);
		}
	}

	public void StartLoadingProcess(DictionaryInfo info, String s3bucket,
			boolean isReload) {

		String suggesterType = info.getInfos().get(Property.SUGGESTER);
		String bucketName = "";
		if ((bucketName = info.getInfos().get(Property.S3_BUCKET_SUFFIX)) != null
				&& bucketName.trim().length() > 0) {
			s3bucket = bucketName;
		}

		suggesterType = suggesterType == null ? Property.SUGGESTER_TYPE.analyzingsuggester
				.toString() : suggesterType;

		log.info("**************************************************************");
		log.info(" Loading dictionary for " + info.getDictionaryName()
				+ "  bucketName " + s3bucket + "  ,Path : "
				+ info.getDictionaryPath());
		log.info("**************************************************************");
		try {

			S3Object s3file = s3Client.getObject(s3bucket,
					info.getDictionaryPath());
			log.info("**************************************************************");
			log.info("Successfully got access to S3 bucket : " + s3bucket);
			log.info("**************************************************************");

			InputStream is = s3file.getObjectContent();

			/********** Important code to work on ************************/

			/***/

			if (suggesterType
					.equalsIgnoreCase(Property.SUGGESTER_TYPE.complexfuzzysuggester
							.toString())) {

				TRAnalyzingSuggesterExt suggester = createComplexFuzzysuggester(is);
				suggesterList.put(info.getDictionaryName(), suggester);

			} else if (suggesterType
					.equalsIgnoreCase(Property.SUGGESTER_TYPE.analyzingsuggester
							.toString())) {
				TRAnalyzingSuggester suggester = createAnalyzingSuggesterForOthers(
						is, new DictionaryEntry());

				suggesterList.put(info.getDictionaryName(), suggester);
			} else if (suggesterType
					.equalsIgnoreCase(Property.SUGGESTER_TYPE.fuzzysuggester
							.toString())) {

				TRAnalyzingSuggesterExt suggester = createSimpleFuzzysuggester(is);
				suggesterList.put(info.getDictionaryName(), suggester);

			} else if (suggesterType
					.equalsIgnoreCase(Property.SUGGESTER_TYPE.companytypeaheadsuggester
							.toString())) {

				CompanyTypeaheadSuggester suggester = createCompanySuggester(is);
				suggesterList.put(info.getDictionaryName(), suggester);

			} else if (suggesterType
					.equalsIgnoreCase(Property.SUGGESTER_TYPE.defaultcomplextypeaheadsuggester
							.toString())) {

				TechnicalTypeaheadSuggester suggester = createTechnicalSuggester(is);
				suggesterList.put(info.getDictionaryName(), suggester);

			}

			/***************************** End **********************************/

			log.info("**************************************************************");
			if (!isReload) {
				log.info("  Loading dictionary for " + info.getDictionaryName()
						+ " completed successfully.");
			} else {

				log.info("  Reloading dictionary for "
						+ info.getDictionaryName() + " completed successfully.");
			}
			log.info("**************************************************************");

		} catch (Exception e) {
			if (!isReload) {
				log.info(" fail loading dictionary for "
						+ info.getDictionaryName());
			} else {
				log.info(" fail reloading dictionary for "
						+ info.getDictionaryName());
			}

			e.printStackTrace();
		}

	}

	/****************************************************************************************/
	/********************* Creating Analyzers starts Here ************************************/
	/****************************************************************************************/

	public TRAnalyzingSuggester createDefaultAnalyzingSuggester(InputStream is)
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

	public TRAnalyzingSuggesterExt createSimpleFuzzysuggester(InputStream is) {
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

	public TRAnalyzingSuggesterExt createComplexFuzzysuggester(InputStream is) {
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

	public CompanyTypeaheadSuggester createCompanySuggester(InputStream is) {
		CompanyTypeaheadSuggester suggester = null;
		try {

			suggester = new CompanyTypeaheadSuggester(is);

			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	public TechnicalTypeaheadSuggester createTechnicalSuggester(InputStream is) {
		TechnicalTypeaheadSuggester suggester = null;
		try {

			suggester = new TechnicalTypeaheadSuggester(is);

			is.close();

			System.gc();
			System.gc();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return suggester;
	}

	public void loadAllDictionaryProperyValues(
			Map<String, DictionaryInfo> dictionaryInfos) {

		Set<String> paths = dictionaryInfos.keySet();

		for (String key : paths) {

			String path = Property.DICTIONARY_PATH + key;
			String DictionaryS3PAth = ConfigurationManager.getConfigInstance()
					.getString(path);

			DictionaryInfo info = dictionaryInfos.get(key);

			info.setDictionaryPath(DictionaryS3PAth);
			Enumeration<String> innerpaths = info.getInfos().keys();
			while (innerpaths.hasMoreElements()) {
				String realProperty = innerpaths.nextElement();
				String extraProperty = path + "." + realProperty;

				String extraValue = ConfigurationManager.getConfigInstance()
						.getString(extraProperty);
				info.getInfos().put(realProperty, extraValue);
			}

			dictionaryInfos.put(key, info);
		}

	}

}
