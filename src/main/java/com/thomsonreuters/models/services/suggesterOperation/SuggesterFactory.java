package com.thomsonreuters.models.services.suggesterOperation;

import java.io.IOException;

import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.thomsonreuters.models.services.suggesters.S3BucketFromS3IAMRole;
import com.thomsonreuters.models.services.suggesters.SuggesterFromS3Bucket;

/**
 * 
 * 
 * @author Manoj Manandhar
 * 
 *         This will create Suggester According to the parameter value
 *         "String:type".
 * 
 *         For e.g.
 * 
 *         For S3 bucket it will create SuggesterFromS3Bucket
 * 
 *         in Future, if we need http handler we can create a class
 *         SuggesterFromHttp.
 * 
 *         if we need ftp handler we can create a class SuggesterFromFtp
 *         instantly and return it.
 *
 */
public class SuggesterFactory {

	public static final DictionaryLoader<Lookup> createSuggesters(
			String type) throws IOException {

		if (type.equals("S3")) {
			return new SuggesterFromS3Bucket();
		} else if (type.equals("S3IAM")) {
			return new S3BucketFromS3IAMRole();
		}

		return new SuggesterFromS3Bucket();
	}

}
