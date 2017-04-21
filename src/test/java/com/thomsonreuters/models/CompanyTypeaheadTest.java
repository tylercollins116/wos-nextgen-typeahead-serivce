package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.models.company.CompanyTypeaheadSuggester;

public class CompanyTypeaheadTest {

	public static void main(String[] args) {

		System.gc();
		System.gc();

		System.out.printf("Avaliable %.3fGB",
				(Runtime.getRuntime().totalMemory())
						/ (1024.0 * 1024.0 * 1024.0));
		System.out.println();
		System.out.printf("Used %.3fGB",
				(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / (1024.0 * 1024.0 * 1024.0));
		System.out.println();
		System.out.printf("FreeMemory %.3fGB",
				(Runtime.getRuntime().freeMemory())
						/ (1024.0 * 1024.0 * 1024.0));
		System.out.println();

	}
	
	private CompanyTypeaheadSuggester suggester=null;
	
	@Before	
	public void loadData() throws Exception {
		
		/*** creating and *********/
		InputStreamGenerator obj = new InputStreamGenerator();
		obj.addLine("{\"id\":\"CNEW_NEW\",\"name\":\"TOKAI EFFECT YG\",\"parents\":\"\",\"count\":\"18\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_3520252\",\"name\":\"WAFANGDIAN CHENGDA BEARING MFR CO LTD\",\"parents\":\"CNEW_NEW\",\"count\":\"1\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_1125736\",\"name\":\"GLACIALPOWER INC\",\"parents\":\"CNEW_NEW\",\"count\":\"1\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_2836251\",\"name\":\"SCHAKO METAL SCAD KG\",\"parents\":\"CNEW_NEW_3520252\",\"count\":\"1\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_1658904\",\"name\":\"KANGMEI XINKAIHE JILIN PHARM CO LTD\",\"parents\":\"CNEW_NEW_2836251\",\"count\":\"13\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_3558530\",\"name\":\"WEINMANN & PARTNER GMBH\",\"parents\":\"CNEW_NEW_1658904\",\"count\":\"12\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_474978\",\"name\":\"CELLMAX LTD\",\"parents\":\"CNEW_NEW_1125736\",\"count\":\"23\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_909659\",\"name\":\"EUREMO SA\",\"parents\":\"CNEW_NEW_474978\",\"count\":\"1\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_2654762\",\"name\":\"RAPID RACK INDUSTRIES, INC.\",\"parents\":\"CNEW_NEW_474978\",\"count\":\"2\"}");
		obj.addLine("{\"id\":\"CHILD\",\"name\":\"GRAND PARENT\",\"parents\":\"\",\"count\":\"2\"}");
		obj.addLine("{\"id\":\"CHILD_1\",\"name\":\"PARENT\",\"parents\":\"CHILD\",\"count\":\"5\"}");
		obj.addLine("{\"id\":\"CHILD_2\",\"name\":\"CHILD\",\"parents\":\"CHILD_1\",\"count\":\"6\"}");
		obj.addLine("{\"id\":\"CHILD_3\",\"name\":\"GRAND CHILD\",\"parents\":\"CHILD_2\",\"count\":\"6\"}");
		obj.addLine("{\"id\":\"CHILD_4_1\",\"name\":\"GRAND GRAND CHILD1\",\"parents\":\"CHILD_3\",\"count\":\"10\"}");
		obj.addLine("{\"id\":\"CHILD_4_2\",\"name\":\"GRAND GRAND CHILD2\",\"parents\":\"CHILD_3\",\"count\":\"50\"}");
		obj.addLine("{\"id\":\"CHILD_5_1\",\"name\":\"THOMSON\",\"parents\":\"CHILD_4_2\",\"count\":\"20\"}");
		obj.addLine("{\"id\":\"HEWLETT-PACKARD\",\"name\":\"HEWLETT-PACKARD\",\"parents\":\"\",\"count\":\"50\"}");
		obj.addLine("{\"id\":\"CHILDREN_4_2\",\"name\":\"CHILDREN\",\"parents\":\"\",\"count\":\"50\"}");
		obj.addLine("{\"id\":\"10001\",\"name\":\"LG LED CO LTD\",\"parents\":\"\",\"count\":\"18\"}");
		obj.addLine("{\"id\":\"10002\",\"name\":\"LG LED CO LTD\",\"parents\":\"10001\",\"count\":\"18\"}");
		obj.addLine("{\"id\":\"C1790_SAMSUNGMOBILE_2799835\",\"name\":\"SAMSUNG    MOBILE DISPLAY CO LTD\",\"parents\":\"C1790_SAMSUNGMOBILE\",\"count\":\"36848\"}");
		obj.addLine("{\"id\":\"C1790_SAMSUNGMOBILE\",\"name\":\"SAMSUNG MOBILE DISPLAY CO LTD\",\"parents\":\"\",\"count\":\"36848\"}");
		obj.addLine("{\"id\":\"CNEW_NEW_947976\",\"name\":\"FEDERAL STATE BUDGETARY INSTITUTION \\\"RESEARCH CENTER OF NEUROLOGY\\\" RUSSIAN ACADEMY OF MEDICAL SCIENCES (FSBI \\\"RCN\\\" RAMS)\",\"parents\":\"\",\"count\":\"1\"}");

		/******************************************/

		Long totalMemory = Runtime.getRuntime().totalMemory();

		  suggester = new CompanyTypeaheadSuggester(
				obj.getStream());
		
	}

	@Test
	public void testCompanyTypeahead() throws Exception { 
		

		String wholetree = (suggester.lookup("PAREN", 10, 2, false, true));
		String expectedResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[{\"name\":\"GRAND CHILD\",\"count\":6,\"clusterId\":\"CHILD_3\",\"children\":[{\"name\":\"GRAND GRAND CHILD2\",\"count\":50,\"clusterId\":\"CHILD_4_2\",\"children\":[{\"name\":\"THOMSON\",\"count\":20,\"clusterId\":\"CHILD_5_1\",\"children\":[]}]},{\"name\":\"GRAND GRAND CHILD1\",\"count\":10,\"clusterId\":\"CHILD_4_1\",\"children\":[]}]}]}]}]}]}";
		assertEquals(expectedResult, wholetree);

		long start = System.currentTimeMillis();

		String result = (suggester.lookup("SCHAK", 10, 2, false, false));
		String expectiondResult = "{\"suggestion\":[{\"name\":\"TOKAI EFFECT YG\",\"count\":18,\"clusterId\":\"CNEW_NEW\",\"children\":[{\"name\":\"WAFANGDIAN CHENGDA BEARING MFR CO LTD\",\"count\":1,\"clusterId\":\"CNEW_NEW_3520252\",\"children\":[{\"name\":\"SCHAKO METAL SCAD KG\",\"count\":1,\"clusterId\":\"CNEW_NEW_2836251\",\"children\":[]}]}]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("GRAND GRAN", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[{\"name\":\"GRAND CHILD\",\"count\":6,\"clusterId\":\"CHILD_3\",\"children\":[{\"name\":\"GRAND GRAND CHILD2\",\"count\":50,\"clusterId\":\"CHILD_4_2\",\"children\":[]},{\"name\":\"GRAND GRAND CHILD1\",\"count\":10,\"clusterId\":\"CHILD_4_1\",\"children\":[]}]}]}]}]}]}";
		assertEquals(expectiondResult, result);

		/** Test for Size **/

		result = (suggester.lookup("CHIL", 2, 2, false, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[{\"name\":\"GRAND CHILD\",\"count\":6,\"clusterId\":\"CHILD_3\",\"children\":[{\"name\":\"GRAND GRAND CHILD2\",\"count\":50,\"clusterId\":\"CHILD_4_2\",\"children\":[]},{\"name\":\"GRAND GRAND CHILD1\",\"count\":10,\"clusterId\":\"CHILD_4_1\",\"children\":[]}]}]}]}]},{\"name\":\"CHILDREN\",\"count\":50,\"clusterId\":\"CHILDREN_4_2\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("CHIL", 2, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[{\"name\":\"GRAND CHILD\",\"count\":6,\"clusterId\":\"CHILD_3\",\"children\":[]}]}]}]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("CHIL", 1, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[]}]}]}]}";
		assertEquals(expectiondResult, result);

		/** Test for HEWLETT-PACK and its different conditions **/

		result = (suggester.lookup("HEWLETT-PACK", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"HEWLETT-PACKARD\",\"count\":50,\"clusterId\":\"HEWLETT-PACKARD\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("PACKA", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"HEWLETT-PACKARD\",\"count\":50,\"clusterId\":\"HEWLETT-PACKARD\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("HEWLETT PAC", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"HEWLETT-PACKARD\",\"count\":50,\"clusterId\":\"HEWLETT-PACKARD\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		/** End Test for HEWLETT-PACK Test ***/

		/**
		 * testing collapse that 2 SAMSUNG LED CO LTD into one , only collapse
		 * when parent and child has same name and has same count
		 **/

		result = (suggester.lookup("LG", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"LG LED CO LTD\",\"count\":18,\"clusterId\":\"10001\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		result = (suggester.lookup("SAM", 10, 2, true, false));
		expectiondResult = "{\"suggestion\":[{\"name\":\"SAMSUNG MOBILE DISPLAY CO LTD\",\"count\":36848,\"clusterId\":\"C1790_SAMSUNGMOBILE\",\"children\":[]}]}";
		assertEquals(expectiondResult, result);

		try {
			result = (suggester.lookup("FEDER", 10, 2, true, false));
		} catch (Exception e) {
			assertNull(e);
		}

	}

	private class InputStreamGenerator {

		private StringBuilder sb = new StringBuilder();

		private final String nextline = "\n";

		public void addLine(String line) {

			if (sb.length() > 0) {
				sb.append(nextline);
			}

			sb.append(line);

		}

		public InputStream getStream() throws IOException {

			Reader initialReader = new StringReader(sb.toString());

			char[] charBuffer = new char[8 * 1024];
			StringBuilder builder = new StringBuilder();
			int numCharsRead;
			while ((numCharsRead = initialReader.read(charBuffer, 0,
					charBuffer.length)) != -1) {
				builder.append(charBuffer, 0, numCharsRead);
			}
			InputStream targetStream = new ByteArrayInputStream(builder
					.toString().getBytes(StandardCharsets.UTF_8));

			initialReader.close();

			return targetStream;
		}

	}
}
