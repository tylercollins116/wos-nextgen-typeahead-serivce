package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;

import com.thomsonreuters.models.services.util.PrepareDictionary;

public class TopicEntry extends Entry {

	private Map<String, String> JsonToMap = null;

	public TopicEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(1);
		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {

		return "";
	}

	public static void main(String[] args) {

		Map<String, String> JsonToMap = PrepareDictionary
				.processJson("{\"keyword\":\"undulation and bio composite\",\"count\":5}");

		KeywordEntry entry = new KeywordEntry(JsonToMap);

		System.out.println(entry.getJson());

	}

}
