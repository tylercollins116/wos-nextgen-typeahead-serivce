package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArticleEntry extends Entry {

	private static final String ID = "id";
	private static final String TITLE = "title";

	private Map<String, String> JsonToMap = null;

	public ArticleEntry(Map<String, String> JsonToMap) {
		setTerm(JsonToMap.remove(Entry.TERM));
		setWeight(Integer.parseInt((JsonToMap.remove(Entry.WEIGHT)).trim()));

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
		
		Map<String, String> datas=new HashMap<String, String>();
		datas.put("keyword","management");
		datas.put("count","63");
		datas.put("id","WOS:000184971100016");
		datas.put("title","Adaptive management of large rivers with special reference to the Missouri River");
		
		ArticleEntry article=new ArticleEntry(datas);
		System.out.println(article.getJson());

		
	}

}
