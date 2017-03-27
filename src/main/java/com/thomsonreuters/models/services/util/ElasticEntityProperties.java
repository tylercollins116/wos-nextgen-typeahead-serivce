package com.thomsonreuters.models.services.util;

import java.util.HashMap;

public class ElasticEntityProperties {

	private String type = "";
	private String[] searchField = null;
	private String[] returnFields = null;
	private HashMap<String, String> aliasFields = null;
	private HashMap<String, String> sortFields = null;
	private String analyzer = "";
	private Integer[] maxExpansion = new Integer[] {};
	private int slop = 3;
	private String host = "";
	private String port="";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getSearchField() {
		return searchField;
	}

	public void setSearchField(String[] searchField) {
		this.searchField = searchField;
	}

	public String[] getReturnFields() {
		return returnFields;
	}

	public void setReturnFields(String[] returnFields) {
		this.returnFields = returnFields;
	}

	public HashMap<String, String> getAliasFields() {
		return aliasFields;
	}

	public void setAliasFields(HashMap<String, String> aliasFields) {
		this.aliasFields = aliasFields;
	}

	public HashMap<String, String> getSortFields() {
		return sortFields;
	}

	public void setSortFields(HashMap<String, String> sortFields) {
		this.sortFields = sortFields;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public Integer[] getMaxExpansion() {
		return maxExpansion;
	}

	public void setMaxExpansion(Integer[] maxExpansion) {
		this.maxExpansion = maxExpansion;
	}

	public int getSlop() {
		return slop;
	}

	public void setSlop(String slops) {

		try {
			this.slop = Integer.parseInt(slops);
		} catch (Exception e) {
			this.slop = 3;
		}

		if (this.slop < 0) {
			this.slop = 3;
		}
		 
	}

	public String getHost(String source) {

		if (this.host != null && this.host.length() > 5 && this.port!=null && this.port.length()>0) {

			String url = "http://" + host+":"+this.port + Property.ES_SEARCH_PATH.get(source) + "/_search";
			return url;
		}

		return null;
	}

	public void setHost(String host) {

		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	

}
