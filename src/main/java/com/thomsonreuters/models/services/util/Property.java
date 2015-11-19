package com.thomsonreuters.models.services.util;

public interface Property {

	public String DICTIONARY_PATH = "dictionary.path.";
	public String S3_BUCKET = "s3.bucket";
	public String AWS_KEY_ID = "aws.key.id";
	public String AWS_KEY = "aws.key";
	
	public String SEARCH_HOST="";
	public String SEARCH_PORT="";
	public String SEARCH_PATH_WOS="";
	public String DEFAULT_TYPEAHEAD_TYPES="typeahead.default.types";

	public static final String FUZZYNESS_THRESHOLD_PATH = "fuzziness.threshold";

	public enum TYPE {
		DICTIONARY_PATH, S3_BUCKET, NONE;
	}

	public String getDictionayName();

	public boolean isDictionaryPathRelated();

	public boolean isBucketName();

}
