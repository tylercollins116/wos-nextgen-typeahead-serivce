package com.thomsonreuters.query.core;

import java.util.Map;

import com.thomsonreuters.query.model.QueryManagerInput;

public class QueryBuilder {

	private QueryBuilder() {}
	
	public static String generate(QueryManagerInput queryManagerInput, String searchField) {

		String analyzer = queryManagerInput.getAnalyzer();
		String coatedQuery = org.codehaus.jettison.json.JSONObject.quote(queryManagerInput.getQueryTerm());

		StringBuilder sb = new StringBuilder();

		for (String field : queryManagerInput.getReturnFields()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(org.codehaus.jettison.json.JSONObject.quote(field));
		}

		String esQuery = "{\"from\":" + queryManagerInput.getFrom() + ",\"size\":" + queryManagerInput.getSize() + ",";

		esQuery += getSortingField(queryManagerInput.getSortFields());

		esQuery += getQueryCore(queryManagerInput.getQueryType(), searchField, coatedQuery, 
				analyzer, queryManagerInput.getSlop(), queryManagerInput.getExpansion());

		esQuery += ",\"fields\":[" + sb.toString() + "]";

		esQuery += "}";
		return esQuery;

	}

	private static String getSortingField(Map<String, String> sortFields) {
		StringBuilder sb = new StringBuilder();
		String prefix = "";

		for (Map.Entry<String, String> entry : sortFields.entrySet()) {
			sb.append(prefix);			
			if ("asc".equalsIgnoreCase(entry.getValue())) {
				sb.append(getSort(entry.getKey(),"asc"));
			} 
			else {
				sb.append(getSort(entry.getKey(),"desc"));
			}
			prefix = ",";
		}

		if (sb.toString().trim().length() > 0) {
			return "\"sort\": [" + sb.toString() + "],";
		}

		return "";
		
	}
	
	private static String getSort(String fieldName, String orderAs) {
		if (fieldName == null || fieldName.trim().length() <= 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("{\"" + fieldName + "\": { \"order\": \"" + orderAs
				+ "\" } }");
		return sb.toString();
	
	}
	
	private static String getQueryCore(String queryType, String searchField, String coatedQuery
			, String analyzer, int slop, int expansion) {

		String query = "\"query\":{";
		
		if("ngrams".equalsIgnoreCase(queryType)) {
			query += "\"match\":{"
					+ org.codehaus.jettison.json.JSONObject.quote(searchField)
					+ ":{\"query\":" + coatedQuery + ","
					+ "\"operator\": \"and\"";
			
			query += getAnalyzer(analyzer);

			query += "}}";

		}
		else {
			query += "\"constant_score\":{\"query\":{\"match_phrase_prefix\":{"
					+ org.codehaus.jettison.json.JSONObject.quote(searchField)
					+ ":{\"query\":" + coatedQuery + ",";

			query += getAnalyzer(analyzer);

			query += "\"slop\":" + slop + ",\"max_expansions\":" + expansion
					+ "}}}}";
		}
		query += "}";

		return query;
	}
	
	private static String getAnalyzer(String analyzer) {
		if (analyzer != null && analyzer.trim().length() > 0) {
			return "\"analyzer\":"
					+ org.codehaus.jettison.json.JSONObject.quote(analyzer)
					+ ",";
		}
		return "";
	}
}
