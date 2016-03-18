package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.thomsonreuters.models.services.util.PrepareDictionary;

public class CategoryEntry extends Entry {

	private static final String CATEGORY = "category";

	private Map<String, String> JsonToMap = null;

	public CategoryEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));
		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {

		 

		StringBuilder sb = new StringBuilder("{");

		if (JsonToMap != null && JsonToMap.size() > 0) {
			Set<String> keys = JsonToMap.keySet();
			for (String key : keys) {

				if (sb.length() > 1) {
					sb.append(",");
				}

				if (key.toLowerCase().equalsIgnoreCase(CATEGORY)) {

					sb.append("\"" + CATEGORY + "\":");
					sb.append("\"" + JsonToMap.get(CATEGORY) + "\"");
				} else {
					sb.append("\"" + key + "\":");
					sb.append("\"" + JsonToMap.get(key) + "\"");
				}

			}

		}

		sb.append("}");

		return sb.toString();
	}

	 
}
