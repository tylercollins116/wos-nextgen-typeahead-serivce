package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.AssertTrue;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;

public class SuggesterHelperTest {
	
	
	@Test
	public void testSuggesterHelper(){
		
		List<String> datas = getValuesToIndex();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		for (String line : datas) {
			try {
				baos.write(line.getBytes());
				baos.write("\r".getBytes());
			} catch (Exception e) {

			}
		}

		byte[] bytes = baos.toByteArray();

		InputStream is = new ByteArrayInputStream(bytes);
		
		SuggesterHelper helper=new SuggesterHelper() {
		};
		
		TRAnalyzingSuggesterExt organizationSuggester=helper.createAnalyzingSuggesterForOrganization(is);
		
		List<Lookup.LookupResult> allResults = null;
		try{
			allResults=organizationSuggester.lookup("ALBA IU", false, 5);
		 
		} catch (Exception e) {
			allResults = null;
		}

		assertNotNull(allResults);
		assertTrue(allResults.size()>0);
		
		System.out.println(allResults.get(0).key.toString());
		assertEquals("ALBA IULIA", allResults.get(0).key.toString());
		
	}
	
	
	
	
	
	private List<String> getValuesToIndex() {
		List<String> list = new ArrayList<String>();
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA IULIA UNIV\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA JULIA\",\"count\":13,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIV ALBA LULIA\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE 1918 UNIVERSITY ALBA IULIA\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE UNIV\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1 DECEMBRIE UNIV ALBA JULIA\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1918\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"1ST DECEMBER 1918 UNIV ALBA IULIA\",\"count\":12,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA IULIA\",\"count\":180,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"ALBA IULIA UNIV\",\"count\":3,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBER 1ST UNIV ALBA IULIA\",\"count\":2,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE\",\"count\":718,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE 1918 UNIV\",\"count\":1,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");
		list.add("{\"keyword\":\"DECEMBRIE 1918 UNIV ALBA JULIA\",\"count\":3,\"alias\":\"1 Decembrie 1918 University Alba Iulia\",\"id\":\"40270007289368\"}");

		return list;
	}

}
