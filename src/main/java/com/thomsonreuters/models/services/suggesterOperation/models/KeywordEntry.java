package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeywordEntry extends Entry {

	private Map<String, String> JsonToMap = null;

	public KeywordEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.get(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.get(Entry.WEIGHT)).trim()));

		this.JsonToMap = JsonToMap;

	}

	@Override
	public String getJson() {

		if (true) {
			return "";
		}

		StringBuilder sb = new StringBuilder("{");		 

		if (JsonToMap != null && JsonToMap.size() > 0) {
			Set<String> keys = JsonToMap.keySet();
			for (String key : keys) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				sb.append("\"" + TERM + "\":");
				sb.append("\"" + JsonToMap.get(TERM) + "\"");

			}

		}

		sb.append("}");

		return sb.toString();
	}

	public static void main(String[] args) {

		Map<String, String> datas = new HashMap<String, String>();
		datas.put("keyword", "management");
		datas.put("count", "63");

		Entry article = new KeywordEntry(datas);
		System.out.println(article.getJson());

	}

}
