package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

public class DictionaryEntry extends Entry { 

	public static final String TERM = "keyword";

	private Map<String, String> JsonToMap = null;
	
	public DictionaryEntry(){}

	public DictionaryEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(TERM));
		
		//Dont put remove for weight because there is no way to get count on result if we remove that
		setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));

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

	
	@Override
	public Entry clone(Map<String, String> JsonToMap) {
		 return new DictionaryEntry(JsonToMap);
	} 
}
