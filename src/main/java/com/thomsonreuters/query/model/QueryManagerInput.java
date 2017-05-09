package com.thomsonreuters.query.model;

import java.util.Map;

import com.thomsonreuters.models.services.util.ElasticEntityProperties;

public class QueryManagerInput {
	
	private String elasticSearchUrl = "";
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
	private int from = 0;
	private int size = 10;
	private String queryTerm = "";
	private String source = "";
	private int maxExpansionPosition = 0;
	private boolean highLight = false;
	private String queryType = "";

	public QueryManagerInput(ElasticEntityProperties eep, int from, int size, String queryTerm, String source, boolean highlight) {
		this.elasticSearchUrl = eep.getElasticSearchUrl(source);
		this.type = eep.getType();
		this.searchField = eep.getSearchField();
		this.returnFields = eep.getReturnFields();
		this.aliasFields = eep.getAliasFields();
		this.sortFields = eep.getSortFields();
		this.analyzer = eep.getAnalyzer();
		this.maxExpansion = eep.getMaxExpansion();
		this.slop = eep.getSlop();
		this.host = eep.getHost();
		this.port = eep.getPort();
		this.from = from;
		this.size = size;
		this.queryTerm = queryTerm;
		this.source = source;
		this.maxExpansionPosition = 0;
		this.highLight = highlight;
		this.setQueryType(eep.getQueryType());
	}

	public String getElasticSearchUrl() {
		return elasticSearchUrl;
	}

	public void setElasticSearchUrl(String elasticSearchUrl) {
		this.elasticSearchUrl = elasticSearchUrl;
	}

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

	public void setSlop(int slop) {
		this.slop = slop;
	}

	public String getHost() {
		return host;
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

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getQueryTerm() {
		return queryTerm;
	}

	public void setQueryTerm(String queryTerm) {
		this.queryTerm = queryTerm;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getMaxExpansionPosition() {
		return maxExpansionPosition;
	}

	public void setMaxExpansionPosition(int maxExpansionPosition) {
		this.maxExpansionPosition = maxExpansionPosition;
	}
	
	public boolean increaseMaxExpansion() {
		this.maxExpansionPosition = this.maxExpansionPosition + 1;
		if(maxExpansionPosition < this.maxExpansion.length)
			return true;
		return false;
	}
	
	public int getExpansion() {
		return this.maxExpansion[this.maxExpansionPosition];
	}

	public boolean isHighLight() {
		return highLight;
	}

	public void setHighLight(boolean highLight) {
		this.highLight = highLight;
	}

	public String getQueryType() {
		return queryType;
	}

	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	
	public boolean isNgramsQuery() {
		if("ngrams".equalsIgnoreCase(queryType)){
			return true;
		}
		return false;
	}
	

}
