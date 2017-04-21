package com.thomsonreuters.models.services.util;

import java.util.Map;

import com.netflix.config.ConfigurationManager;

public class ElasticEntityProperties {

	private String type = "";
	private String[] searchField = null;
	private String[] returnFields = null;
	private Map<String, String> aliasFields = null;
	private Map<String, String> sortFields = null;
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

	public Map<String, String> getAliasFields() {
		return aliasFields;
	}

	public void setAliasFields(Map<String, String> aliasFields) {
		this.aliasFields = aliasFields;
	}

	public Map<String, String> getSortFields() {
		return sortFields;
	}

	public void setSortFields(Map<String, String> sortFields) {
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

	public String getHost() {
		return this.host;
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
	
	public String getElasticSearchUrl(String source) {
		String indexPath = Property.ES_SEARCH_PATH.get(source);
		if (this.host != null && this.host.length() > 5 && this.port!=null && this.port.length()>0) {
			return "http://" + host+":"+this.port + "/"+ indexPath + "/_search";
		}
		else {
			String genericHost = ConfigurationManager.getConfigInstance().getString(Property.SEARCH_HOST);
			String genericPort = ConfigurationManager.getConfigInstance().getString(Property.SEARCH_PORT);

			return "http://" + genericHost + ":" + genericPort + "/"+ indexPath + "/_search";
		}
	}

}
