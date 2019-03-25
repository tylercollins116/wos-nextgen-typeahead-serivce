package com.thomsonreuters.query.core;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.query.model.QueryManagerInput;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class QueryMarshallerV5 {

	private static final Logger log = LoggerFactory.getLogger(QueryMarshallerV5.class);

	private QueryMarshallerV5(){}
	
	public static SuggestData parse(QueryManagerInput queryManagerInput, String queryResult){

		SuggestData data = null;
		try {
			data = formatResponse(queryManagerInput, queryResult);
		} catch (JSONException e) {
			log.error("Marshaller error (parse):", e.getMessage(), e);
		}

		return data;

	}

	private static SuggestData mergeFinalResult(SuggestData[] data) {
		Set<String> uniqueTerms = new HashSet<>();
		SuggestData first = null;

		int firstIndex = -1;
		while ( firstIndex < data.length) {
			++firstIndex;
			if (first == null) {
				first = data[firstIndex];
			}
			if (first != null) {
				break;
			}
		}

		if (first != null) {
			for (Suggestions suggestion : first.suggestions) {
				List<Info> infos = suggestion.info;
				for (Info term : infos) {
					/**
					 * term_string is important in here its necessary to do
					 * hardcoding in here
					 **/
					if (term.key.equalsIgnoreCase("term_string")) {
						uniqueTerms.add(term.value);
					}
				}
			}

			for (int i = firstIndex + 1; i < data.length; i++) {
				first.hits = first.hits + data[i].hits;
				for (Suggestions suggestion : data[i].suggestions) {

					List<Info> infos = suggestion.info;
					for (Info term : infos) {
						/**
						 * term_string is important in here its necessary to
						 * do hardcoding in here
						 **/
						if (term.key.equalsIgnoreCase("term_string")) {
							if (!uniqueTerms.contains(term.value)) {
								first.suggestions.add(suggestion);
								uniqueTerms.add(term.value);
							}

						}
					}

				}

			}

		} else {
			first = new SuggestData();
		}
		
		return first;
	}
	
	private static SuggestData formatResponse(QueryManagerInput queryManagerInput, String response) throws JSONException {

		SuggestData suggestData = new SuggestData();
		suggestData.source = queryManagerInput.getSource();

		if(response == null) {
			return suggestData;
		}

		JSONObject sonObj = new JSONObject(response);

		JSONObject obj = sonObj.getJSONObject("hits");
		
		suggestData.hits = obj.getInt("total");

		JSONArray objs = obj.getJSONArray("hits");

		for (int seq_1 = 0; seq_1 < objs.length(); seq_1++) {

			Suggestions suggestions = suggestData.new Suggestions();
			suggestions.keyword = queryManagerInput.getQueryTerm();

			Object obj_ = objs.get(seq_1);
			JSONObject finalObj = new JSONObject(obj_.toString());
			String fielddata = finalObj.getString("_source");

			for (String field : queryManagerInput.getReturnFields()) {
				try {
					String[] subfields = null;
					if (field.contains(".")) {
						subfields =  field.split("\\.");
					} else {
						subfields =  new String[] { field };
					}
					
					JSONObject fieldObject = new JSONObject(fielddata);
					Object fieldValue = null;
					if (subfields.length == 1) {
						if (fieldObject.has(subfields[0])) {
							fieldValue = fieldObject.get(subfields[0]);
						}
					} else {
						for (String sfield : subfields) {
							if (fieldObject.has(sfield)) {
								Object nodeObj = fieldObject.get(sfield);
								if (nodeObj instanceof JSONObject) {
									fieldObject = (JSONObject) nodeObj;
								} else {
									fieldValue = nodeObj.toString();
									break;
								}
							}
						}
					}
					
					if (fieldValue != null) {

						Info info = suggestData.new Info();

						String aliasFieldName = null;

						if (queryManagerInput.getAliasFields() != null
								&& queryManagerInput.getAliasFields().size() > 0
								&& (aliasFieldName = queryManagerInput.getAliasFields()
										.get(field)) != null
								&& aliasFieldName.trim().length() > 0) {
							info.key = aliasFieldName;
						} else {
							info.key = field;
						}

						info.value = fieldValue.toString() + "";
						suggestions.info.add(info);
					}
				} catch (Exception e) {
					log.error("Marshaller error (formatResponse):", e.getMessage(), e);
				}
			}
			if(queryManagerInput.isHighLight()) {
				String highLight = getHighLight(finalObj, queryManagerInput.getSearchField()[0]);
				if(highLight != null) {
					suggestions.highlight = highLight;
				}
			}
			suggestData.suggestions.add(suggestions);
		}

		return suggestData;
	}
	
	private static String getHighLight(JSONObject finalObj, String searchField) {
		try {
			JSONObject highlightObject = new JSONObject(finalObj.getString("highlight"));
			return highlightObject.getJSONArray(searchField).getString(0);
		} catch (Exception e) {
			log.error("Marshaller error (getHighLight):", e.getMessage(), e);
			return null; 
		}
	}


}