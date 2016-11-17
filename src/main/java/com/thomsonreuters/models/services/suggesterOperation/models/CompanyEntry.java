package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CompanyEntry extends Entry implements Iterator<Entry> {

	OrganizationEntry entry = new OrganizationEntry();
	Map<String, String> JsonToMap = null;

	// {"id":"ROCKSTARBIDCO","variations":[],"parents":["RPXCLEARING","RPXCORP"],"children":["ROCKSTAR
	// BIDCO LP","ROCKSTAR CONSORTIUM US LP"]}

	private final String ID = "id";
	private final String PARENTS = "parents";
	private final String VARIATIONS = "variations";
	private final String CHILDREN = "children";
	private final String NAME = "name";
	private final String ALIAS = "alias";
	private final String COUNT = "count";

	List<Map<String, String>> allInfos = new ArrayList<Map<String, String>>();

	public CompanyEntry() {
	}

	public CompanyEntry(Map<String, String> JsonToMap) {

		String[] terms = new String[] {};
		try {
			String value = JsonToMap.get(VARIATIONS);
			if (value != null && value.trim().length() > 0) {
				terms = value.split(DELIMETER);
			}
		} catch (Exception e) {
		}

		String children = get(CHILDREN, JsonToMap.get(CHILDREN));
		String parent = get(PARENTS, JsonToMap.get(PARENTS));
		String id = JsonToMap.get(ID);
		String name = null;
		String count = JsonToMap.get(COUNT);
		try {
			name = JsonToMap.get(NAME);
		} catch (Exception e) {
			name = null;
		}
		if (name == null) {
			name = id;
		}

		for (String term : terms) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(TERM, term);
			map.put(WEIGHT, "1");
			map.put(CHILDREN, children);
			map.put(PARENTS, parent);
			map.put("NAME", term);
			//map.put("NAME", name);
			map.put(ALIAS, name);
			map.put(COUNT, count);
			allInfos.add(map);
		}

		Map<String, String> map = new HashMap<String, String>();
		if(id==null){
			id=name;
		}
		map.put(TERM, id);
		map.put(WEIGHT, "1");
		map.put(CHILDREN, children);
		map.put(PARENTS, parent);
		map.put("NAME", name);
		map.put(ALIAS, name);
		map.put(COUNT, count);
		allInfos.add(map);

	}

	@Override
	public String getJson() {
		return "";
	}

	@Override
	public Entry clone(Map<String, String> JsonToMap) {
		return new CompanyEntry(JsonToMap);
	}

	@Override
	public boolean hasNext() {

		try {
			this.JsonToMap = allInfos.remove(0);
			if (JsonToMap != null) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}

		return false;
	}

	@Override
	public Entry next() {
		return entry.clone(JsonToMap);
	}

	private String get(String key, String data) {

		StringBuilder sb = new StringBuilder();
		sb.append("[");

		String[] terms = new String[] {};
		try {
			if (data.length() > 0) {
				if (data.indexOf(DELIMETER) > 0) {
					terms = data.split(DELIMETER);
				} else {
					terms = new String[] { data };
				}
			}
		} catch (Exception e) {
			terms = new String[] {};
		}

		for (String term : terms) {
			if (sb.length() > 2) {
				sb.append(",");
			}
			sb.append("\"" + term + "\"");

		}
		sb.append("]");

		return sb.toString();
	}

}
