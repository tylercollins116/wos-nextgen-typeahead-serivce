package com.thomsonreuters.models.services.suggesterOperation;

public interface IProcessPreSearchTerm {
	
	public String[] getPreSearchedTerm(String truid, String... info);
	 
	
	public String[] getSuggestions(String[] terms,String query);
	

}
