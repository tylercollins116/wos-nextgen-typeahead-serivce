package com.thomsonreuters.models;

import java.util.Enumeration;

import org.junit.Test;

import com.thomsonreuters.models.services.util.BlockingHashTable;

import static org.junit.Assert.*;

public class BlockingHashTableTest {
	
	@Test
	public void testThis(){
		BlockingHashTable<String, String> table=new BlockingHashTable<String, String>();
		table.put("key1", "value");
		table.put("key2", "value");
		String value=table.get("key1");
		
		assertEquals("value", value);
		
		Enumeration<String> keys= table.getKeys();
		
		assertTrue(keys.hasMoreElements());
		
	}

}
