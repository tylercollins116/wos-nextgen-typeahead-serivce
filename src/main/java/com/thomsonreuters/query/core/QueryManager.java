package com.thomsonreuters.query.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.query.model.QueryManagerInput;

public class QueryManager {

	private static final Logger log = LoggerFactory.getLogger(QueryManager.class);
	
	private QueryManager() {}
	
	public static SuggestData query(QueryManagerInput queryManagerInput) {
		
		List<Pair<String, String>> queries = new ArrayList<>(); 
		String[] searchFields = queryManagerInput.getSearchField();
		
		for(String searchField : searchFields) {
			String query = QueryBuilder.generate(queryManagerInput, searchField);
			log.info("{} -> {}", searchField, query);
			queries.add(Pair.of(searchField, query));
		}
		
		List<Pair<String, String>> results = QueryExecutor.execute(queries, queryManagerInput.getElasticSearchUrl());
		if(queryManagerInput.getEsMainVersion() <= 2)
			return QueryMarshaller.parse(queryManagerInput, results);
		else
			return QueryMarshallerV5.parse(queryManagerInput, results);
		
	}
	
}
