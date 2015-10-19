package com.thomsonreuters.models.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.thomsonreuters.models.services.suggesterOperation.models.ArticleEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.CategoryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;

public class PrepareDictionary {

	private static final Logger log = LoggerFactory
			.getLogger(PrepareDictionary.class);

	public static List<com.thomsonreuters.models.services.suggesterOperation.models.Entry> initDictonary(
			InputStream is, Class entryClass) throws IOException {
		log.info("**************************************************************");
		log.info("Pre-initilalizing Started");
		log.info("**************************************************************");
		List<com.thomsonreuters.models.services.suggesterOperation.models.Entry> EntryList = new ArrayList<com.thomsonreuters.models.services.suggesterOperation.models.Entry>();

		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String jsonAsLine = null;

		while ((jsonAsLine = br.readLine()) != null) {
			Map<String, String> jsonToMap = processJson(jsonAsLine); 

			try {
				if (entryClass == OrganizationEntry.class) {

					com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = new OrganizationEntry(
							jsonToMap);
					EntryList.add(entry);

				} else if (entryClass == ArticleEntry.class) {

					com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = new ArticleEntry(
							jsonToMap);
					EntryList.add(entry);

				} else if (entryClass == KeywordEntry.class) {

					com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = new KeywordEntry(
							jsonToMap);
					EntryList.add(entry);
				} else if (entryClass == CategoryEntry.class) {

					com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = new CategoryEntry(
							jsonToMap);
					EntryList.add(entry);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		log.info("**************************************************************");
		log.info("Pre-initilalizing Completed Successfully");
		log.info("**************************************************************");

		return EntryList;
	}

	public static Map<String, String> processJson(String Json) {

		JsonParser parser = new JsonParser();

		Map<String, String> map = new HashMap<String, String>();

		try {

			JsonObject root = (JsonObject) parser.parse(Json);

			for (Entry<String, JsonElement> property : root.entrySet()) {

				Object jsonpart = property.getValue();

				if (jsonpart instanceof JsonPrimitive) {

					JsonPrimitive value = ((JsonPrimitive) jsonpart);

					if (value.isString()) {
						map.put(property.getKey(), value.getAsString());
					} else if (value.isNumber()) {
						map.put(property.getKey(), value.getAsInt() + "");
					}

				}
			}
		} catch (Exception e) {

		}

		return map;

	}

	public static void main(String[] args) {
		Map<String, String> map = PrepareDictionary
				.processJson("{\"keyword\":\"underage\",\"count\":20,\"id\":\"WOS:000283688100012\",\"title\":\"Why Might Adverse Childhood Experiences Lead to Underage Drinking Among US Youth? Findings From an Emergency Department-Based Qualitative Pilot Study\"}");

		Set<String> keys = map.keySet();

		for (String key : keys) {
			System.out.println(key + "\t\t" + map.get(key));
		}

		ArticleEntry article = new ArticleEntry(map);
		System.out.println(article.getJson());
	}

}
