package com.thomsonreuters.models.services.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class GroupTerms implements Property{ 

	public  void groupTermsBasedOnDictionary(List<String> alldictionaryInfo,Map<String, DictionaryInfo> allProperty) {

	 

		for (String info : alldictionaryInfo) {
			info = info.toLowerCase();
			String[] infos = getDictionaryInfo(info);

			if (infos.length == 1) {
				DictionaryInfo propertyInfo = allProperty.get(infos[0]);
				if (propertyInfo == null) {
					propertyInfo = new DictionaryInfo();
					propertyInfo.setDictionaryName(infos[0]);
					allProperty.put(infos[0], propertyInfo);
				}
			}

			if (infos.length > 1) {
				DictionaryInfo propertyInfo = allProperty.get(infos[0]);
				if (propertyInfo == null) {
					propertyInfo = new DictionaryInfo();
					propertyInfo.setDictionaryName(infos[0]);
				}
				propertyInfo.add(infos[1], "");
				allProperty.put(infos[0], propertyInfo);
			}

		}
 
	}

	public   String[] getDictionaryInfo(String data) {

		data = data.replace(Property.DICTIONARY_PATH, "");

		String[] infos = data.split("\\.");

		return infos;

	}
	
	

	public DictionaryInfo getAllDictionaryRelatedInfos(Hashtable<String, DictionaryInfo> allProperty, String property) {
		if (property == null || property.trim().length() <= 0) {
			return null;
		}
		property = property.toLowerCase();
		String[] properties = getDictionaryInfo(property);
		return allProperty.get(properties[0]);

	}
	
	public boolean isDictionaryRelated(String value) {
		if (value.toLowerCase().startsWith(Property.DICTIONARY_PATH)&& (!value.toLowerCase().endsWith(Property.SUGGESTER))) {
			return true;
		}
		return false;
	}
	
	public String getDictionayName(String property){
		
		return getDictionaryInfo(property)[0];
	}
	
	
	 
	@Override
	public boolean isBucketName(String Key) {
		return Key.toLowerCase().trim().equals(Property.S3_BUCKET);
	}

}