package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.Map;
import java.util.Set;

import com.thomsonreuters.models.services.util.PrepareDictionary;

public class ArticleEntry extends Entry {

	public static final String TERM = "title";

	private Map<String, String> JsonToMap = null;

	public ArticleEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(TERM));
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

	public static void main(String[] args) {

		Map<String, String> JsonToMap = PrepareDictionary
				.processJson("{\"keyword\":\"adsorption\",\"count\":11,\"UT\":\"WOS:000337985600052\",\"title\":\"Adsorption behavior and mechanism of perfluorinated compounds on various adsorbents-A review\",\"fuid\":\"472494968WOS1\"}");

		ArticleEntry entry = new ArticleEntry(JsonToMap);
		System.out.println(entry.getJson());

	}

}
