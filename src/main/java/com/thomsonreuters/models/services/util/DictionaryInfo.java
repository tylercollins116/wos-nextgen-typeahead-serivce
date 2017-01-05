package com.thomsonreuters.models.services.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

 

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

	public boolean compare(DictionaryInfo info_) {
		
		if(this.dictionaryName!=null && info_.dictionaryName!=null){
			if(!this.dictionaryName.trim().equalsIgnoreCase(info_.dictionaryName.trim())){
				return false;
			}
		}else if((this.dictionaryName!=null && info_.dictionaryName==null) ||(this.dictionaryName==null && info_.dictionaryName!=null)){
			return false;
		} 
		
		if(this.dictionaryPath!=null && info_.dictionaryPath!=null){
			if(!this.dictionaryPath.trim().equalsIgnoreCase(info_.dictionaryPath.trim())){
				return false;
			}
		}else if((this.dictionaryPath!=null && info_.dictionaryPath==null) ||(this.dictionaryPath==null && info_.dictionaryPath!=null)){
			return false;
		} 
		
		
		Enumeration<String> keystmp = info_.getInfos().keys();
		Enumeration<String> originalkeys = this.infos.keys();
		
		List<String> tempkeyslist=Collections.list(keystmp);
		List<String> originalkeyslist=Collections.list(originalkeys);
		if (tempkeyslist.size() != originalkeyslist.size()) {
			return false;
		}

		for(String key:originalkeyslist) { 
			String orginalValues = this.infos.get(key);
			String tempValues = info_.getInfos().get(key);
			if (tempValues == null) {
				return false;
			}
			if (!orginalValues.toLowerCase().trim().equals(tempValues.toLowerCase().trim())) {
				return false;
			}

		}

		return true;
	}



}
