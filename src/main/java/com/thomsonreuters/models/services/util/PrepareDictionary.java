package com.thomsonreuters.models.services.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class PrepareDictionary
		implements
		Iterator<com.thomsonreuters.models.services.suggesterOperation.models.Entry> {

	private static final Logger log = LoggerFactory
			.getLogger(PrepareDictionary.class);

	private static final List<com.thomsonreuters.models.services.suggesterOperation.models.Entry> entries = new ArrayList<com.thomsonreuters.models.services.suggesterOperation.models.Entry>();

	private String jsonAsLine = "";
	private final BufferedReader br;
	private final com.thomsonreuters.models.services.suggesterOperation.models.Entry entryClass;

	public PrepareDictionary(
			InputStream is,
			com.thomsonreuters.models.services.suggesterOperation.models.Entry entryClass) {
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
						
						try{
						Integer.parseInt(value.getAsString());
						map.put(property.getKey(), value.getAsInt() + "");
						}catch(Exception e){
							map.put(property.getKey(), value.getAsDouble() + "");							
						} 
					}

				} else if (jsonpart instanceof JsonArray) {
					StringBuilder sb = new StringBuilder();
					JsonArray array = (JsonArray) jsonpart;

					for (int i = 0; i < array.size(); i++) {
						if (sb.length() > 1) {
							sb.append(com.thomsonreuters.models.services.suggesterOperation.models.Entry.DELIMETER);
						}
						sb.append(array.get(i).getAsString());
					}

					map.put(property.getKey(), sb.toString());

				}
			}
		} catch (Exception e) {
			 
			e.printStackTrace();
			log.error("error in Json cannot parse the json line "+Json);

		}

		return map;

	}

	@Override
	public boolean hasNext() {

		if (entries.size() > 0) {
			return true;
		}

		if (br == null) {
			return false;
		}
		try {
			if ((jsonAsLine = br.readLine()) != null) {
				process();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	private void process() {

		Map<String, String> jsonToMap = processJson(jsonAsLine);

		com.thomsonreuters.models.services.suggesterOperation.models.Entry entry = null;
		try {

			if (entryClass instanceof Iterator) {
				com.thomsonreuters.models.services.suggesterOperation.models.Entry entrys = entryClass
						.clone(jsonToMap);
				Iterator<com.thomsonreuters.models.services.suggesterOperation.models.Entry> allEntries = (Iterator<com.thomsonreuters.models.services.suggesterOperation.models.Entry>) entrys;

				while (allEntries.hasNext()) {
					entries.add(allEntries.next());
				}
			} else {
				entries.add(entryClass.clone(jsonToMap));

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public com.thomsonreuters.models.services.suggesterOperation.models.Entry next() {

		return entries.remove(0);
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
