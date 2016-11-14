package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;

public class OrganizationEntry extends Entry {

	private final String ALIAS = "alias";

	private Map<String, String> JsonToMap = null;
	
	public OrganizationEntry(){}

	public OrganizationEntry(Map<String, String> JsonToMap) {

		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));

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
				if (key.toLowerCase().equals(ALIAS)) {
					sb.append("\"" + TERM + "\":");
					sb.append("\"" + JsonToMap.get(key) + "\"");
					alias=JsonToMap.get(key);
				} else {

					sb.append("\"" + key + "\":");
					String value=JsonToMap.get(key);
					if(value.startsWith("[") && value.endsWith("]")){
					sb.append(JsonToMap.get(key)); //Treate as array
					}else{
						sb.append("\"" + JsonToMap.get(key) + "\"");
					}
				}
			}

		}

		sb.append("}");

		return alias+TRAnalyzingSuggesterExt.deliminator+sb.toString();
	}
	
	
	@Override
	public Entry clone(Map<String, String> JsonToMap) {
		 return new OrganizationEntry(JsonToMap);
	}

}

