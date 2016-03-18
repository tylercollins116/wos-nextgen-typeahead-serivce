package com.thomsonreuters.models.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
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
import com.thomsonreuters.models.services.suggesterOperation.models.PatentEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.PeopleEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.TopicEntry;

public class PrepareDictionary
		implements
		Iterator<com.thomsonreuters.models.services.suggesterOperation.models.Entry> {

	private static final Logger log = LoggerFactory
			.getLogger(PrepareDictionary.class);

	private String jsonAsLine = "";
	private final BufferedReader br;
	private final Class entryClass;

	public PrepareDictionary(InputStream is, Class entryClass) {
		this.entryClass = entryClass;
		br = new BufferedReader(new InputStreamReader(is));

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

	

	@Override
	public boolean hasNext() {

		if (br == null) {
			return false;
		}
		try {
			if ((jsonAsLine = br.readLine()) != null) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public com.thomsonreuters.models.services.suggesterOperation.models.Entry next() {

		Map<String, String> jsonToMap = processJson(jsonAsLine);

		com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = null;
		try {
			if (entryClass == OrganizationEntry.class) {

				entry = new OrganizationEntry(jsonToMap);

			} else if (entryClass == TopicEntry.class) {

				entry = new TopicEntry(jsonToMap);

			} else if (entryClass == ArticleEntry.class) {

				entry = new ArticleEntry(jsonToMap);

			} else if (entryClass == KeywordEntry.class) {

				entry = new KeywordEntry(jsonToMap);

			} else if (entryClass == CategoryEntry.class) {

				entry = new CategoryEntry(jsonToMap);

			} else if (entryClass == PeopleEntry.class) {

				entry = new PeopleEntry(jsonToMap);

			} else if (entryClass == PatentEntry.class) {

				entry = new PatentEntry(jsonToMap);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return entry;

	}
	
	public void close() {

		if (br != null) {
			try {
				br.close();

			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

}
