package com.thomsonreuters.models.services.util;

public interface Blockable<String, K> {

	public K put(String key, K value);

	public K get(Object key);

	public void removeObject(String key);

}
