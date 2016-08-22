package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;

import com.thomsonreuters.models.services.util.PrepareDictionary;

public class TopicEntry extends Entry {

	private Map<String, String> JsonToMap = null;
	
	public TopicEntry(){}

	public TopicEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		try {
			setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));
		} catch (Exception e) {
			setWeight(1);
		}

		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {
		return "";
	} 

	
	@Override
	public Entry clone(Map<String, String> JsonToMap) {
		 return new TopicEntry(JsonToMap);
	}
	
}
