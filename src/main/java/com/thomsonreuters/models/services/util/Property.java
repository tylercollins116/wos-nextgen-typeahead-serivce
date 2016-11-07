package com.thomsonreuters.models.services.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List; 
import java.util.Map;
public interface Property {

	public String DICTIONARY_PATH = "dictionary.path.";
	
	
	public String AWS_KEY_ID = "aws.key.id";
	public String AWS_KEY = "aws.key";
	
	//Dictionary Related
	public String S3_BUCKET = "s3.bucket";
	public String S3_BUCKET_SUFFIX = "s3";
	public String SUGGESTER = "suggester";
	public String PRESEARCHED_TERMS="presearch";
	
	
	
	//ES related parameters
	public String SEARCH_HOST = "search.host";
	public String SEARCH_PORT = "search.port";
	public String SEARCH_PATH_PREFIX = "search.path";
	 
	
	

	/** this FUZZYNESS_THRESHOLD will define a fuzzyness threshhold**/
	public String FUZZTNESS_THRESHOLD = "fuzzyness.threshold";	
	public int DEFAULT_FUZZTNESS_THRESHOLD = 10;

	public static final HashMap<String, String> ES_SEARCH_PATH = new HashMap<String, String>();

	public static final HashMap<String, String> DICTIONARY_PATHS = new HashMap<String, String>();

	public enum TYPE {
		DICTIONARY_PATH, S3_BUCKET, NONE;
	}

	public enum SUGGESTER_TYPE {
		fuzzysuggester,complexfuzzysuggester, analyzingsuggester
	}

	public String getDictionayName(String property);
	
	boolean isDictionaryRelated(String property);
 
	public boolean isBucketName(String Key); 
	
	public  void groupTermsBasedOnDictionary(List<String> alldictionaryInfo,Map<String, DictionaryInfo> allProperty);

 
//	public static final String article = "articles";
//	public static final String people = "people";
//	public static final String post = "posts";
//	public static final String patent = "patents";
//	public static final String wos = "wos";
//	public static final String category = "categories";
//	public static final String organization = "organizations";
//	public static final String topic = "topics";
//	public static final String company = "company";
//	public static final String companytest = "companytest";
 
}
