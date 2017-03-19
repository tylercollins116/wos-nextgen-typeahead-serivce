package com.thomsonreuters.models.services.suggesterOperation.models;

import java.util.HashMap;
import java.util.Map;

public abstract class Entry {

	public static final String TERM = "keyword";
	protected static final String WEIGHT = "count";
	public static final String DELIMETER = "::";

	private static final HashMap<String, String> termfield = new HashMap<String, String>();
	
	//These are the static values and will same for all the sub classes
	static {
		termfield.put("c", "count");
		termfield.put("k", "keyword");
		termfield.put("i", "id");
		termfield.put("p", "parents");
		termfield.put("ch", "children");

		termfield.put("count", "c");
		termfield.put("keyword", "k");
		termfield.put("id", "i");
		termfield.put("parents", "p");
		termfield.put("children", "ch");
	}

	private String term;

	private int weight;

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public abstract String getJson();

	public abstract Entry clone(Map<String, String> JsonToMap);
	
	 

}
