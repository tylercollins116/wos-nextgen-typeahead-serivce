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

import static java.util.Arrays.asList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.search.suggest.analyzing.AnalyzingInfixSuggester;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.apache.lucene.search.suggest.analyzing.FuzzySuggester;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
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
import com.thomsonreuters.models.services.util.ItemIterator;
import com.thomsonreuters.models.services.util.Item;
import com.thomsonreuters.models.services.util.JsonReader;

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

		// for (String key : keys) {
		// log.info("Founds following dictionary already loaded \"" + key
		// + "\" from path " + dictionaryInfos.get(key));
		// }

		log.info("***************************************************************************");

	}

	public void initializeSuggesterList() throws IOException {
		////////////////////////////////////////////////////////
		////////////// Make a lookup object instance ///////////
		// In this case it will be an AnalyzingInfixSuggester //
		////////////////////////////////////////////////////////
		// Define filename
		String fn = "/Users/tylercollins/Documents/example.json";

		// Read in from file and add to entities
		JsonReader jsonReader = new JsonReader();
		ItemIterator entitiesIter = null;
		try {
			entitiesIter = jsonReader.ReadJSON(fn);
		} catch (Exception e){
			System.out.println(e);
		}

		// Build analyzing infix suggester object
		final RAMDirectory indexDir = new RAMDirectory();
		final WhitespaceAnalyzer analyzer = new WhitespaceAnalyzer();
		Lookup lookupObj = new AnalyzingInfixSuggester(indexDir, analyzer, analyzer, 1, true);
		lookupObj.build(entitiesIter);

		// Put an entry in the suggester list
		suggesterList.put("prefixMatch", lookupObj);


		// Build analyzing suggester object
		final WhitespaceAnalyzer analyzer2 = new WhitespaceAnalyzer();
		Lookup lookupObj2 = new AnalyzingSuggester(analyzer2);
		lookupObj2.build(entitiesIter);

		// Put an entry in the suggester list
		suggesterList.put("anyPartMatch", lookupObj2);


		// Build analyzing suggester object
		final WhitespaceAnalyzer analyzer3 = new WhitespaceAnalyzer();
		Lookup lookupObj3 = new FuzzySuggester(analyzer3);
		lookupObj3.build(entitiesIter);

		// Put an entry in the suggester list
		suggesterList.put("fuzzyMatch", lookupObj3);

//		Iterator<String> keys = ConfigurationManager.getConfigInstance()
//				.getKeys();
//
//
//		System.out.println("\n--------------\nPRINT CONFIG KEYS");
//		while (keys.hasNext())
//			System.out.println(keys.next());
//		System.out.println("--------------\n");
//
//
//		if (s3Client == null) {
//			throw new IOException(
//					" S3Client found null pls initilize s3Client first");
//		}
//
//		String bucketName = null;
//
//		Property property = new GroupTerms();
//
//		List<String> allDictionaryRelatedInfos = new ArrayList<String>();
//
//		while (keys.hasNext()) {
//			String key = keys.next();
//
//			if (property.isDictionaryRelated(key)) {
//				allDictionaryRelatedInfos.add(key);
//			}
//		}
//
//		property.groupTermsBasedOnDictionary(allDictionaryRelatedInfos,
//				dictionaryInfos);
//
//		loadAllDictionaryProperyValues(dictionaryInfos);
//
//		keys = ConfigurationManager.getConfigInstance().getKeys();
//
//		while (keys.hasNext()) {
//			String key = keys.next();
//			if (property.isBucketName(key)) {
//				bucketName = ConfigurationManager.getConfigInstance()
//						.getString(key);
//				log.info("path to bucket : " + bucketName);
//
//			}
//		}
//
//		Set<String> dictionaries = dictionaryInfos.keySet();
//
//		for (String key : dictionaries) {
//
//			DictionaryInfo info = dictionaryInfos.get(key);
//
//			getStoredPathInfo();
//
//			String s3bucket = info.getInfos().get(property.S3_BUCKET_SUFFIX);
//
//			s3bucket = s3bucket == null ? bucketName : s3bucket;
//
//			String s3Path = info.getDictionaryPath();
//
//			if (s3bucket == null) {
//				s3bucket = bucketName;
//			}
//
//			StartLoadingProcess(info, bucketName, false);
//
//		}

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

			if (realDictionaryInfo == null) {
				log.info("Reloading new dictionary " + dictionaryName);
			} else {
				log.info("updating  dictionary " + dictionaryName);
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

			// if loading fails then this will help to fail the healthcheck
			suggesterList.put(info.getDictionaryName(), createDefaultSuggester());
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
	
	
	public Lookup createDefaultSuggester(){
		return new Lookup(){

			@Override
			public long ramBytesUsed() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public long getCount() throws IOException {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public void build(InputIterator inputIterator) throws IOException {
				// TODO Auto-generated method stub
				
			}

			@Override
			public List<LookupResult> lookup(CharSequence key,
					Set<BytesRef> contexts, boolean onlyMorePopular, int num)
					throws IOException {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public boolean store(DataOutput output) throws IOException {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean load(DataInput input) throws IOException {
				// TODO Auto-generated method stub
				return false;
			}};
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
				String suggester = Property.SUGGESTER;
				/**
				 * Above line doesnt have any effect but will mark here as we
				 * will get the suggester type from eiddo
				 **/
				String extraValue = ConfigurationManager.getConfigInstance()
						.getString(extraProperty);
				info.getInfos().put(realProperty, extraValue);
			}

			dictionaryInfos.put(key, info);
		}

	}
}
