package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.suggest.Lookup;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry;

public class SuggesterHelperTest {

	@Test
	public void testCreateAnalyzingSuggesterForOrganization() {

		List<String> datas = getValuesToIndex1();

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

		SuggesterHelper helper = new SuggesterHelper() {
		};

		TRAnalyzingSuggesterExt organizationSuggester = helper
				.createAnalyzingSuggesterForOrganization(is);

		List<Lookup.LookupResult> allResults = null;
		try {
			allResults = organizationSuggester.lookup("ALBA IU", false, 5);

		} catch (Exception e) {
			allResults = null;
		}

		assertNotNull(allResults);
		assertTrue(allResults.size() > 0);

		 
		assertEquals("ALBA IULIA", allResults.get(0).key.toString());

	}

	@Test
	public void testCreateAnalyzingSuggesterForOthers() {

		List<String> datas = getValuesToIndex2();

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

		SuggesterHelper helper = new SuggesterHelper() {
		};

		TRAnalyzingSuggester otherSuggester = helper
				.createAnalyzingSuggesterForOthers(is, KeywordEntry.class);

		List<Lookup.LookupResult> allResults = null;
		try {
			allResults = otherSuggester.lookup("scr", false, 5);

		} catch (Exception e) {
			allResults = null;
		}

		assertNotNull(allResults);
		assertTrue(allResults.size() > 0);

		 
		assertEquals("scrapie", allResults.get(0).key.toString());

	}

	private List<String> getValuesToIndex1() {
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

	private List<String> getValuesToIndex2() {
		List<String> list = new ArrayList<String>();
		list.add("{\"keyword\":\"growth\",\"count\":40137}");
		list.add("{\"keyword\":\"growth analysis\",\"count\":282}");
		list.add("{\"keyword\":\"growth and yield\",\"count\":107}");
		list.add("{\"keyword\":\"growth arrest\",\"count\":241}");
		list.add("{\"keyword\":\"growth cone\",\"count\":340}");
		list.add("{\"keyword\":\"habitat\",\"count\":3889}");
		list.add("{\"keyword\":\"habitat complexity\",\"count\":228}");
		list.add("{\"keyword\":\"habitat fragmentation\",\"count\":2192}");
		list.add("{\"keyword\":\"habitat heterogeneity\",\"count\":233}");
		list.add("{\"keyword\":\"habitat loss\",\"count\":574}");
		list.add("{\"keyword\":\"habitat management\",\"count\":175}");
		list.add("{\"keyword\":\"masseter muscle\",\"count\":127}");
		list.add("{\"keyword\":\"mast cells\",\"count\":5387}");
		list.add("{\"keyword\":\"mastectomy\",\"count\":678}");
		list.add("{\"keyword\":\"master equation\",\"count\":297}");
		list.add("{\"keyword\":\"mastication\",\"count\":338}");
		list.add("{\"keyword\":\"mastitis\",\"count\":3008}");
		list.add("{\"keyword\":\"matched filter\",\"count\":227}");
		list.add("{\"keyword\":\"matching\",\"count\":2207}");
		list.add("{\"keyword\":\"measure of noncompactness\",\"count\":167}");
		list.add("{\"keyword\":\"measurement\",\"count\":18874}");
		list.add("{\"keyword\":\"measurement error\",\"count\":1550}");
		list.add("{\"keyword\":\"measurement invariance\",\"count\":251}");
		list.add("{\"keyword\":\"scrapie\",\"count\":541}");
		return list;
	}

}
