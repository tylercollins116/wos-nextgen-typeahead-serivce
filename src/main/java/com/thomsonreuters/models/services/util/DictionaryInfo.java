package com.thomsonreuters.models.services.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

 

public class DictionaryInfo {


	private String dictionaryName;
	private String dictionaryPath;
	private Hashtable<String, String> infos = new Hashtable<String, String>();

	public Hashtable<String, String> getInfos() {
		return infos;
	}

 
	
	
	public String getDictionaryName() {
		return dictionaryName;
	}




	public void setDictionaryName(String dictionaryName) {
		this.dictionaryName = dictionaryName;
	}




	public String getDictionaryPath() {
		return dictionaryPath;
	}




	public void setDictionaryPath(String dictionaryPath) {
		this.dictionaryPath = dictionaryPath;
	}




	public void add(String key, String value) {
		infos.put(key, value);
	}

	public String getValue(String key) {
		return this.infos.get(key);
	}

	public boolean compare(DictionaryInfo info) {
		Enumeration<String> keystmp = info.getInfos().keys();
		Enumeration<String> originalkeys = this.infos.keys();
		if (Collections.list(keystmp).size() != Collections.list(originalkeys).size()) {
			return false;
		}

		while (originalkeys.hasMoreElements()) {
			String key = originalkeys.nextElement();
			String orginalValues = this.infos.get(key);
			String tempValues = info.getInfos().get(key);
			if (tempValues == null) {
				return false;
			}
			if (orginalValues.toLowerCase().trim().equals(tempValues.toLowerCase().trim())) {
				return false;
			}

		}

		return true;
	}



}
