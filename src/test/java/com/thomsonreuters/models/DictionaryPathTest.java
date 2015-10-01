package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
 

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class DictionaryPathTest {

	@Parameters
	public static Collection<String[]> data() {

		List<String[]> list = new ArrayList<String[]>();
		list.add(new String[]{"dictionary.path.profile","profile"});
		list.add(new String[]{"dictionary.path.wos","wos"});
		list.add(new String[]{"dictionary.path.country","country"});
		return list;
		

	}
	
	private String data;
	private String expectedOutput;
	
	public DictionaryPathTest(String data,String expectedOutput){
		this.data=data;
		this.expectedOutput=expectedOutput;
		
		
	}
	
	@Test
	public void test(){
		assertEquals(com.thomsonreuters.models.services.util.PropertyValue.getProperty(data).getDictionayName(), expectedOutput);		
	}
	

}
