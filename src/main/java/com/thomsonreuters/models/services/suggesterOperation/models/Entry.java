package com.thomsonreuters.models.services.suggesterOperation.models;

public abstract class Entry {

	public static final String TERM = "keyword";
	protected static final String WEIGHT = "count";
	

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

}
