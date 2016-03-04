package com.thomsonreuters.models;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

public class ListTest {

	@Test
	public void doTest() {
		
		List<suggesterForTest> allSuggester=new ArrayList<ListTest.suggesterForTest>();
		allSuggester.add(new suggesterForTest("key"));
		allSuggester.add(new suggesterForTest("value"));
		allSuggester.add(new suggesterForTest("java"));
		allSuggester.add(new suggesterForTest("groovy"));
		allSuggester.add(new suggesterForTest("csharp"));
		
		suggesterForTest obj=new suggesterForTest("groovy"); 
		
		assertTrue(allSuggester.contains(obj));

	}

	class suggesterForTest {

		public String keyword;

		public suggesterForTest(String keyword) {
			this.keyword = keyword;
		}
		
	 
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof suggesterForTest){
				
				String keyword=null;
				
				if((keyword=((suggesterForTest)obj).keyword)!=null && keyword.equalsIgnoreCase(this.keyword)){
					return true;
				}
			}
			return false;
		}

	}

}
