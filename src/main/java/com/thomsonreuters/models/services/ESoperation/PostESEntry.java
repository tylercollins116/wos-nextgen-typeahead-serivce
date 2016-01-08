package com.thomsonreuters.models.services.ESoperation;

import java.util.HashMap;

import com.thomsonreuters.models.services.ESoperation.IQueryGenerator.orderAs;
import com.thomsonreuters.models.services.ESoperation.IQueryGenerator.sort;
import com.thomsonreuters.models.services.util.Property;

public class PostESEntry extends IQueryGenerator {

	private final String returnFields[];
	public String query = "";

	private int from = 0;
	private int size = 4;
	private static final String type = Property.post;
	private final String source; // for type article the content type is wos so
									// it must be different then type

	public PostESEntry(String[] returnFields, String userQuery, int from,
			int size, String source, HashMap<String, String> aliasFields) {
		super(type, returnFields);
		this.returnFields = returnFields;
		this.query = userQuery;
		this.from = from;
		this.size = size;
		this.source = source;
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
		super.sorts.add(new sort("sortdate", orderAs.desc));
		return generatESQuery("title", from, size, query, returnFields);
	}

	@Override
	public String getQuery() {
		return this.query;
	}

}
