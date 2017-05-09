package com.thomsonreuters.query.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.query.model.QueryManagerInput;


public class QueryMarshaller {
	
	private static final Logger log = LoggerFactory.getLogger(QueryMarshaller.class);

	private QueryMarshaller(){}
	
	public static SuggestData parse(QueryManagerInput queryManagerInput, List<Pair<String, String>> queryResults){

		SuggestData[] data = new SuggestData[queryResults.size()];

		for (int i = 0; i < queryResults.size(); i++) {
			
			try {
				data[i] = formatResponse(queryManagerInput, queryResults.get(i).getLeft(), queryResults.get(i).getRight());
			} catch (JSONException e) {
				log.error("Marshaller error (parse):", e.getMessage(), e);
			}			
		}
		if (data.length == 0) {
			return new SuggestData();
		} else if (data.length == 1) {
			return data[0];
		}
		else {
			return mergeFinalResult(data);
		}

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
	
	private static SuggestData formatResponse(QueryManagerInput queryManagerInput, String searchField, String response) throws JSONException {

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
			String fielddata = finalObj.getString("fields");

			for (String field : queryManagerInput.getReturnFields()) {

				try {

					JSONObject fieldObject = new JSONObject(fielddata);

					if (!fieldObject.has(field)) {
						fieldObject.put(field, new JSONArray());
					}

					JSONArray fieldValue = fieldObject.getJSONArray(field);

					if (fieldValue.length() > 0) {

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

						info.value = fieldValue.get(0) + "";
						suggestions.info.add(info);
					}
				} catch (Exception e) {
					log.error("Marshaller error (formatResponse):", e.getMessage(), e);
				}
			}
			if(queryManagerInput.isHighLight()) {
				String highLight = getHighLight(finalObj, searchField);
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