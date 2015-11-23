package com.thomsonreuters.models.services.ESoperation;

public class ArticleESEntry extends IQueryGenerator {

	private final String returnFields[];
	public String query = "";

	private int from = 0;
	private int size = 10;
	private static final String type = "article";
	private final String source; // for type article the content type is wos so
									// it must be different then type

	public ArticleESEntry(String[] returnFields, String userQuery, int from,
			int size, String source) {
		super(type, returnFields);
		this.returnFields = returnFields;
		this.query = userQuery;
		this.from = from;
		this.size = size;
		this.source = source;
		super.analyzer="en_std_syn";
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
		return generatESQuery("title", from, size, query, returnFields);
	}

	@Override
	public String getQuery() {
		return this.query;
	}

}
