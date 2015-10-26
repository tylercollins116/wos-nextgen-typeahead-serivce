package com.thomsonreuters.models.services.util;

import java.util.Enumeration;
import java.util.Set;

public interface Blockable<String, K> {

	public K put(String key, K value);

	public K get(Object key);
	
	public Enumeration<String> getKeys();
	
	

	 
}
