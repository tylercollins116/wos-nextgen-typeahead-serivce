package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

public class TechnologyEntry extends Entry{
	
	private Map<String, String> JsonToMap = null;
	
	public TechnologyEntry(){}
	 
	public TechnologyEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.get("term_string"));
		setWeight(Integer.parseInt((JsonToMap.get("term_count")).trim()));

		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {	
		String alias=null;

		StringBuilder sb = new StringBuilder("{");

		if (JsonToMap != null && JsonToMap.size() > 0) {
			Set<String> keys = JsonToMap.keySet();
			for (String key : keys) {
				if (sb.length() > 1) {
					sb.append(",");
				} 
				
				 
					sb.append("\"" + key + "\":");
					sb.append("\"" + JsonToMap.get(key) + "\"");
					alias=JsonToMap.get(key);
				 
			}

		}

		sb.append("}");
		
		return sb.toString();
}

	@Override
	public Entry clone(Map<String, String> JsonToMap) {
		 return new  TechnologyEntry(JsonToMap);
	}

}
