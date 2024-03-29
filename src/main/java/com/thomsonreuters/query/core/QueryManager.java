package com.thomsonreuters.query.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.query.model.QueryManagerInput;

public class QueryManager {

	private static final Logger log = LoggerFactory.getLogger(QueryManager.class);
	
	private QueryManager() {}
	
	public static SuggestData query(QueryManagerInput queryManagerInput) {

		String query = QueryBuilder.generate(queryManagerInput);

		String result = QueryExecutor.execute(query, queryManagerInput.getElasticSearchUrl());
		if(queryManagerInput.getEsMainVersion() <= 2)
			return QueryMarshaller.parse(queryManagerInput, result);
		else
			return QueryMarshallerV5.parse(queryManagerInput, result);
		
	}
	
}
