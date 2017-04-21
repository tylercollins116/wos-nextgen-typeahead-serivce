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
		
		String parsedSearchField = org.codehaus.jettison.json.JSONObject.quote(searchField);

		String esQuery = "{\"from\":" + queryManagerInput.getFrom() + ",\"size\":" + queryManagerInput.getSize() + ",";

		esQuery += getSortingField(queryManagerInput.getSortFields());

		esQuery += "\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{"
				+ parsedSearchField
				+ ":{\"query\":" + coatedQuery + ",";

		if (analyzer != null && analyzer.trim().length() > 0) {
			esQuery += "\"analyzer\":"
					+ org.codehaus.jettison.json.JSONObject.quote(analyzer)
					+ ",";
		}

		esQuery += "\"slop\":" + queryManagerInput.getSlop() + ",\"max_expansions\":" + queryManagerInput.getExpansion()
				+ "}}}}},\"fields\":[" + sb.toString() + "]";

		if(queryManagerInput.isHighLight()) {
			esQuery += ",\"highlight\": {\"fields\": { " + parsedSearchField + ": {}}}";
			
		}
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
}
