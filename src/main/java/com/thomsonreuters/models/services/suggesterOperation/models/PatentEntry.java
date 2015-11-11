package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

public class PatentEntry extends Entry {

	private Map<String, String> JsonToMap = null;

	public PatentEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(1);

		this.JsonToMap = JsonToMap;
	}

	@Override
	public String getJson() {
		StringBuilder sb = new StringBuilder("{");
		boolean keyWordExist = false;

		if (JsonToMap != null && JsonToMap.size() > 0) {
			Set<String> keys = JsonToMap.keySet();
			for (String key : keys) {

				if (sb.length() > 1) {
					sb.append(",");
				}

				sb.append("\"" + key + "\":");
				sb.append("\"" + JsonToMap.get(key) + "\"");
			}

		}

		sb.append("}");

		return sb.toString();
	}

}
