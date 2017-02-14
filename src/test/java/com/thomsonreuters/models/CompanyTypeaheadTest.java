package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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

	@Test
	public void testCompanyTypeahead() throws Exception {

		Long totalMemory = Runtime.getRuntime().totalMemory();

		CompanyTypeaheadSuggester suggester = new CompanyTypeaheadSuggester(
				ClassLoader.class
						.getResourceAsStream("/sampledict/company.dict"));

		String wholetree = (suggester.lookup("GRA", 10, 2, false, true));
		String expectedResult = "{\"suggestion\":[{\"name\":\"GRAND PARENT\",\"count\":2,\"clusterId\":\"CHILD\",\"children\":[{\"name\":\"PARENT\",\"count\":5,\"clusterId\":\"CHILD_1\",\"children\":[{\"name\":\"CHILD\",\"count\":6,\"clusterId\":\"CHILD_2\",\"children\":[{\"name\":\"GRAND CHILD\",\"count\":6,\"clusterId\":\"CHILD_3\",\"children\":[{\"name\":\"GRAND GRAND CHILD2\",\"count\":50,\"clusterId\":\"CHILD_4_2\",\"children\":[]},{\"name\":\"GRAND GRAND CHILD1\",\"count\":10,\"clusterId\":\"CHILD_4_1\",\"children\":[]}]}]}]}]}]}";
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
}
