package com.thomsonreuters.query.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;
import com.thomsonreuters.models.services.util.Property;

public class QueryManager {

	
	private QueryManager() {}
	
	public static SuggestData query(ElasticEntityProperties eep, int from, int size, int expansion, String queryTerm, String source) {
		
		List<Pair<String, String>> queries = new ArrayList<>(); 
		String[] searchFields = eep.getSearchField();
		
		for(String searchField : searchFields) {
			String query = QueryBuilder.generate(eep, searchField, from, size, expansion, queryTerm);
			queries.add(Pair.of(searchField, query));
		}
		
		List<Pair<String, String>> results = QueryExecutor.execute(queries, getElasticSearchUrl(eep, source));
		
		return QueryMarshaller.parse(results, eep, queryTerm, source);
		
	}
	
	private static String getElasticSearchUrl(ElasticEntityProperties eep, String source) {
		String customUrl = eep.getHost(source);
		if (customUrl != null && customUrl.length() > 4) {
			return customUrl;
		} else {
			String esurl = ConfigurationManager.getConfigInstance().getString(Property.SEARCH_HOST);
			String port = ConfigurationManager.getConfigInstance().getString(Property.SEARCH_PORT);

			return "http://" + esurl + ":" + port + "/"+ Property.ES_SEARCH_PATH.get(source)+ "/_search";
		}
	}
}
