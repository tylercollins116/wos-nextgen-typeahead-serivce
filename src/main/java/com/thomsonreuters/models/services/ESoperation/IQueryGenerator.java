package com.thomsonreuters.models.services.ESoperation;

import java.util.HashMap;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;

public abstract class IQueryGenerator {

	protected final String type;
	private final String[] returnFields;
	protected String analyzer = null;
	private String query = "";
	protected HashMap<String, String> aliasFields = null;

	private static final Logger logger = LoggerFactory
			.getLogger(IQueryGenerator.class);

	public IQueryGenerator(String type, String[] returnFields) {
		this.type = type;
		this.returnFields = returnFields;
	}

	private String response = "";

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public abstract String createQuery();

	public abstract String getSource();

	public abstract String getQuery();

	protected String generatESQuery(String searchField, int from, int size,
			String query, String[] returnFields) {

		String coatedQuery = org.codehaus.jettison.json.JSONObject.quote(query);

		StringBuilder sb = new StringBuilder();

		for (String field : returnFields) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(org.codehaus.jettison.json.JSONObject.quote(field));
		}

		/**
		 * -----------not working--------------
		 * {"from":0,"size":10,"query":{"constant_score"
		 * :{"query":{"match":{"title"
		 * :{"query":"exact linear det","type":"phrase_prefix"
		 * ,"analyzer":"en_std_syn","slop":1,"max_expansions":50}}}}},"fields":[
		 * "fullrecord.summary.title","cuid","fuid"]}
		 * 
		 * 
		 * 
		 * ----------working----------------------------
		 * {"from":0,"size":10,"query"
		 * :{"constant_score":{"query":{"match_phrase_prefix"
		 * :{"title":{"query":"exact linear det"
		 * ,"slop":3,"analyzer":"en_std_syn"
		 * ,"max_expansions":50}}}}},"fields":["title"
		 * ,"fullrecord.summary.title","cuid","fuid"]}
		 * ---------------------------------------------
		 */

		String esQuery = "{\"from\":"
				+ from
				+ ",\"size\":"
				+ size
				+ ",\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{"
				+ org.codehaus.jettison.json.JSONObject.quote(searchField)
				+ ":{\"query\":" + coatedQuery + ",";

		if (this.analyzer != null && this.analyzer.trim().length() > 0) {
			esQuery += "\"analyzer\":"
					+ org.codehaus.jettison.json.JSONObject.quote(analyzer)
					+ ",";
		}
		esQuery += "\"slop\":3,\"max_expansions\":50}}}}},\"fields\":["
				+ sb.toString() + "]}";

		System.out.println(esQuery);

		logger.info(esQuery);
		return esQuery;

	}

	public SuggestData formatResponse() throws JSONException {

		SuggestData suggestData = new SuggestData();
		suggestData.source = type;

		JSONObject sonObj = new JSONObject(getResponse());

		JSONObject obj = sonObj.getJSONObject("hits");

		JSONArray objs = obj.getJSONArray("hits");

		for (int seq_1 = 0; seq_1 < objs.length(); seq_1++) {

			Suggestions suggestions = suggestData.new Suggestions();
			suggestions.keyword = getQuery();

			Object obj_ = objs.get(seq_1);
			JSONObject finalObj = new JSONObject(obj_.toString());
			String fielddata = (finalObj.getString("fields"));

			for (String field : returnFields) {

				try {

					JSONArray fieldValue = new JSONObject(fielddata)
							.getJSONArray(field);

					if (fieldValue.length() > 0) {

						Info info$ = suggestData.new Info();

						String aliasFieldName = null;

						if (this.aliasFields != null
								&& this.aliasFields.size() > 0
								&& (aliasFieldName = this.aliasFields
										.get(field)) != null
								&& aliasFieldName.trim().length() > 0) {
							info$.key = aliasFieldName;
						} else {
							info$.key = field;
						}

						info$.value = fieldValue.get(0) + "";
						suggestions.info.add(info$);
					}
				} catch (Exception e) {
				}
			}

			suggestData.suggestions.add(suggestions);
		}

		return suggestData;
	}

	/**
	 * public String generateQuery(String query, int from, int size) {
	 * 
	 * MatchQueryBuilder matchQueryBuilder = QueryBuilders
	 * .matchPhrasePrefixQuery("title", query); matchQueryBuilder.slop(1);
	 * 
	 * if (analyzer != null && analyzer.trim().length() > 0) {
	 * matchQueryBuilder.analyzer(analyzer); }
	 * 
	 * ConstantScoreQueryBuilder csb = QueryBuilders
	 * .constantScoreQuery(matchQueryBuilder);
	 * 
	 * SearchSourceBuilder queryBuilder = new SearchSourceBuilder();
	 * queryBuilder.version(true);
	 * 
	 * queryBuilder.query(csb);
	 * queryBuilder.size(size).from(from).fields(returnFields);
	 * 
	 * return queryBuilder.toString(); }
	 **/
}
