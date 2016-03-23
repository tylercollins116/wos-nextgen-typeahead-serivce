package com.thomsonreuters.models;

import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.IProcessPreSearchTerm;
import com.thomsonreuters.models.services.suggesters.ProcessPreSearchTerm;

import static org.junit.Assert.*;

public class PreSearchedTermsTest {
	
	@Test
	public void testPreSearchedTerms(){
		IProcessPreSearchTerm terms=new ProcessPreSearchTerm();
		String[] preSearchTerms=terms.getPreSearchedTerm("4d20d64c-dccb-4637-bd36-92083bd18911");
		assertEquals(0, preSearchTerms.length);
		
	}

}
