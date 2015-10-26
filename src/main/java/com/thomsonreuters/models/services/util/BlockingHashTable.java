package com.thomsonreuters.models.services.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

public class BlockingHashTable<String, K> extends Hashtable<String, K>
		implements Blockable<String, K> {

	private final Object lock = new Object();

	private boolean requireLock = false;

	@Override
	public K put(String key, K value) {
		synchronized (lock) {
			this.requireLock = true;
			super.remove(key);
			K k = super.put(key, value);
			this.requireLock = false;
			return k;
		}

	}

	@Override
	public K get(Object key) {

		if (!requireLock) {
			return super.get(key);
		} else {
			synchronized (lock) {
				return super.get(key);
			}

		}
	}

	@Override
	public Enumeration<String> getKeys() {

		return this.keys();
	}

}
