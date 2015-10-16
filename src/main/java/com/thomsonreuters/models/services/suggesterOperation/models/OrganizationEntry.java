package com.thomsonreuters.models.services.suggesterOperation.models;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class OrganizationEntry extends Entry{

	private final String ALIAS = "alias";
	 
	private Map<String, String> JsonToMap = null;

	public OrganizationEntry(Map<String, String> JsonToMap) {

		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.remove(Entry.WEIGHT)).trim()));
		 
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
				if (key.toLowerCase().equals(ALIAS)) {
					sb.append("\""+TERM+"\":");
					sb.append("\"" + JsonToMap.get(key) + "\"");
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
