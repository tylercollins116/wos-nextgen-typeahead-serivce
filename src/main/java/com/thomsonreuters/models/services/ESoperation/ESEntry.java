package com.thomsonreuters.models.services.ESoperation;

import java.util.HashMap;
import java.util.Map;

public class ESEntry extends IQueryGenerator {

	private final String returnFields[];
	public String query = "";

	private int from = 0;
	private int size = 4;
	private final String source;
	private String searchField;
	private HashMap<String, String> sortFields;

	public ESEntry(String type, String[] returnFields, String userQuery, int from, int size, String source,
			HashMap<String, String> aliasFields, String analyzer, String searchField,
			HashMap<String, String> sortFields) {
		super(type, returnFields);
		this.returnFields = returnFields;
		this.query = userQuery;
		this.from = from;
		this.size = size;
		this.source = source;
		super.analyzer = analyzer;
		this.aliasFields = aliasFields;
		this.searchField = searchField;
		this.sortFields = sortFields;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public String getSource() {
		// TODO Auto-generated method stub
		return this.source;
	}

	@Override
	public String createQuery() {
		if (this.sortFields != null && this.sortFields.size() > 0) {
			for (Map.Entry<String, String> entry : this.sortFields.entrySet()) {
				if ("asc".equalsIgnoreCase(entry.getValue())) {
					super.sorts.add(new sort(entry.getKey(), orderAs.asc));
				} else {
					super.sorts.add(new sort(entry.getKey(), orderAs.desc));
				}
			}
		}
		return generatESQuery(this.searchField, from, size, query, returnFields);
	}

	@Override
	public String getQuery() {
		return this.query;
	}
}
