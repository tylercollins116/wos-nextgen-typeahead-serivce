package com.thomsonreuters.query.core;

import java.util.Map;

import com.thomsonreuters.models.services.util.ElasticEntityProperties;

public class QueryBuilder {

	private QueryBuilder() {}
	
	public static String generate(ElasticEntityProperties eep, String searchField
			, int from, int size, int expansion, String queryTerm) {

		String analyzer = eep.getAnalyzer();
		String coatedQuery = org.codehaus.jettison.json.JSONObject.quote(queryTerm);

		StringBuilder sb = new StringBuilder();

		for (String field : eep.getReturnFields()) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(org.codehaus.jettison.json.JSONObject.quote(field));
		}

		String esQuery = "{\"from\":" + from + ",\"size\":" + size + ",";

		esQuery += getSortingField(eep.getSortFields());

		esQuery += "\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{"
				+ org.codehaus.jettison.json.JSONObject.quote(searchField)
				+ ":{\"query\":" + coatedQuery + ",";

		if (analyzer != null && analyzer.trim().length() > 0) {
			esQuery += "\"analyzer\":"
					+ org.codehaus.jettison.json.JSONObject.quote(analyzer)
					+ ",";
		}

		esQuery += "\"slop\":" + eep.getSlop() + ",\"max_expansions\":" + expansion
				+ "}}}}},\"fields\":[" + sb.toString() + "]}";

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
