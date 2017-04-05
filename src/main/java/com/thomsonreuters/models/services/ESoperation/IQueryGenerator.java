package com.thomsonreuters.models.services.ESoperation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	protected List<sort> sorts = new ArrayList<IQueryGenerator.sort>();
	private final String[] searchFields;
	protected boolean highLight;

	private int max_expansion = 50;

	private static final Logger logger = LoggerFactory
			.getLogger(IQueryGenerator.class);

	public IQueryGenerator(String type, String[] searchFields, String[] returnFields) {
		this.type = type;
		this.searchFields = searchFields;
		this.returnFields = returnFields;
	}

	private String response = "";

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setMax_expansion(int max_expansion) {
		if (max_expansion <= 0) {
			max_expansion = 50;
		}
		this.max_expansion = max_expansion;
	}

	public abstract String[] createQuery();

	public abstract String getSource();

	public abstract String getQuery();

	public abstract String getESURL();

	protected String generatESQuery(int queryIndex, int from, int size,
			String query, String[] returnFields, int slop) {

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

		/**
		 * { "from": 0, "size": 10, "sort": [ { "inf": { "order": "desc" } },
		 * "_score" ], "query": { "multi_match": { "query": "rec", "type":
		 * "phrase_prefix", "fields": [ "term_search", "term_analyze" ],
		 * "slop": 0, "max_expansions": 4000 } }, "fields": [ "term_string",
		 * "term_count", "inf" ] }
		 * 
		 * 
		 */

		/**
		 * "sort": [ { "citingsrcscount": { "order": "desc" } } ],
		 * 
		 */

		String parsedSearchField = org.codehaus.jettison.json.JSONObject.quote(searchFields[queryIndex]);
		String esQuery = "{\"from\":" + from + ",\"size\":" + size + ",";

		esQuery += getSortingField();

		esQuery += "\"query\":{\"constant_score\":{\"query\":{\"match_phrase_prefix\":{"
				+ parsedSearchField
				+ ":{\"query\":" + coatedQuery + ",";

		if (this.analyzer != null && this.analyzer.trim().length() > 0) {
			esQuery += "\"analyzer\":"
					+ org.codehaus.jettison.json.JSONObject.quote(analyzer)
					+ ",";
		}

		esQuery += "\"slop\":" + slop + ",\"max_expansions\":" + max_expansion
				+ "}}}}},\"fields\":[" + sb.toString() + "]";

		if(highLight) {
			esQuery += ",\"highlight\": {\"fields\": { " + parsedSearchField + ": {}}}";
			
		}
		esQuery += "}";

		// logger.info("ES Query " + esQuery);
		// System.out.println("ES Query " + esQuery);

		return esQuery;

	}

	public String getSortingField() {

		StringBuilder sb = new StringBuilder();
		if (sorts.size() > 0) {
			for (sort sort : sorts) {

				if (sb.length() > 2) {
					sb.append(",");
				}

				sb.append(sort.toString());
			}

			if (sb.toString().trim().length() > 0) {
				return "\"sort\": [" + sb.toString() + "],";
			}

		}
		return "";

	}

	public SuggestData formatResponse(int queryIndex) throws JSONException {

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

					JSONObject fieldObject = new JSONObject(fielddata);

					if (!fieldObject.has(field)) {
						fieldObject.put(field, new JSONArray());
					}

					JSONArray fieldValue = fieldObject.getJSONArray(field);

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
					e.printStackTrace();
				}
			}
			String highLight = getHighLight(finalObj, queryIndex);
			if(highLight != null) {
				suggestions.highlight = highLight;
			}
			suggestData.suggestions.add(suggestions);
		}

		return suggestData;
	}
	
	private String getHighLight(JSONObject finalObj, int queryIndex) {
		try {
			JSONObject highlightObject = new JSONObject((finalObj.getString("highlight")));
			return highlightObject.getJSONArray(this.searchFields[queryIndex]).getString(0);
		} catch (Exception e) { return null; }
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

	public enum orderAs {
		asc, desc;
	}

	public class sort {

		private String sortFieldName;

		private orderAs orderas = orderAs.desc;

		public sort(String sortFieldName_, orderAs orderas_) {
			this.sortFieldName = sortFieldName_;
			this.orderas = orderas_;
		}

		@Override
		public String toString() {
			if (sortFieldName == null || sortFieldName.trim().length() <= 0) {
				return "";
			}
			StringBuilder sb = new StringBuilder();
			sb.append("{\"" + sortFieldName + "\": { \"order\": \"" + orderas
					+ "\" } }");
			return sb.toString();
		}

	}

	/**
	 * get the total count of hits
	 * 
	 * @return total count of hits
	 * @throws JSONException
	 */
	public long getTotalCount() throws JSONException {
		JSONObject sonObj = new JSONObject(getResponse());
		JSONObject obj = sonObj.getJSONObject("hits");
		long count = obj.getLong("total");
		return count;
	}
}
