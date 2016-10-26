package com.thomsonreuters.models.services.ESoperation;

import java.util.HashMap;
import java.util.Map;

import com.thomsonreuters.models.services.util.ElasticEntityProperties;

public class ESEntry extends IQueryGenerator {

	
	

	private int from = 0;
	private int size = 4;
	private final String source;
	public String query = "";
	
	/*
	
	private String searchField;
	private HashMap<String, String> sortFields;
	private int slop = 3;
	
	**/
	ElasticEntityProperties eep=null;

	public ESEntry(ElasticEntityProperties eep, String userQuery,
			int from, int size, String source) {
		super(eep.getType(), eep.getReturnFields());
		
		this.eep=eep;
		this.query = userQuery;	
		this.from = from;
		this.size = size;
		this.source = source;
		
		/**
		this.returnFields = eep.getReturnFields();
			
		super.analyzer = eep.getAnalyzer();
		this.aliasFields = eep.getAliasFields();
		this.searchField = eep.getSearchField();
		this.sortFields = eep.getSortFields();
		if (eep.getSlop() >= 0) {
			this.slop = eep.getSlop();
		}
		
		**/
		 
		
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
		if (eep.getSortFields() != null && eep.getSortFields().size() > 0) {
			for (Map.Entry<String, String> entry : eep.getSortFields().entrySet()) {
				if ("asc".equalsIgnoreCase(entry.getValue())) {
					super.sorts.add(new sort(entry.getKey(), orderAs.asc));
				} else {
					super.sorts.add(new sort(entry.getKey(), orderAs.desc));
				}
			}
		}
		return generatESQuery(eep.getSearchField(), from, size, query, eep.getReturnFields(),eep.getSlop());
	}

	@Override
	public String getQuery() {
		return this.query;
	}

	@Override
	public String getESURL() {
		return this.eep.getHost(this.source);
	}
}
