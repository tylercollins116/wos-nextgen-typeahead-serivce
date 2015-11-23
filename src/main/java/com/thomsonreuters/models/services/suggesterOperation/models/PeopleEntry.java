package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

import com.thomsonreuters.models.services.util.PrepareDictionary;

public class PeopleEntry extends Entry {

	private Map<String, String> JsonToMap = null;

	public PeopleEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));

		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {

		 if(JsonToMap.containsKey("image")){
			 JsonToMap.remove("image");
		 }

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

	public static void main(String[] args) {

		Map<String, String> JsonToMap = PrepareDictionary
				.processJson("{\"keyword\":\"Ginoux, A Paul\",\"count\":1,\"id\":\"ee3faaf5-1486-4cb5-9a29-88468106e1f8\",\"institution\":\"NOAA\",\"image\":\"http://example.com/imageName.jpg\",\"country\":\"USA\",\"role\":\"Chaplain\"}");

		PeopleEntry entry = new PeopleEntry(JsonToMap);
		System.out.println(entry.getJson());

	}

}