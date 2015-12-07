package com.thomsonreuters.models.services.ESoperation;

import java.util.HashMap;

import com.thomsonreuters.models.services.ESoperation.IQueryGenerator.orderAs;
import com.thomsonreuters.models.services.ESoperation.IQueryGenerator.sort;

public class PatentESEntry extends IQueryGenerator {

	private final String returnFields[];
	public String query = "";

	private int from = 0;
	private int size = 10;
	private static final String type = "patent";
	private final String source; // for type article the content type is wos so
									// it must be different then type
	
	

	public PatentESEntry(String[] returnFields, String userQuery, int from,
			int size, String source, HashMap<String, String> aliasFields) {
		super(type, returnFields);
		this.returnFields = returnFields;
		this.query = userQuery;
		this.from = from;
		this.size = size;
		this.source = source;
		super.analyzer="en_std_syn";
		super.aliasFields = aliasFields;

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
		super.sorts.add(new sort("citingsrcscount", orderAs.desc));
		return generatESQuery("title", from, size, query, returnFields);
	}

	@Override
	public String getQuery() {
		return this.query;
	}

}
