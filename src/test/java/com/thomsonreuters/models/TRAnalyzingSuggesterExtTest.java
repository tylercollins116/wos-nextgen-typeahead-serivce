package com.thomsonreuters.models;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.junit.Test;

import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggesterExt;
import com.thomsonreuters.models.services.suggesterOperation.models.EntryIterator;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

public class TRAnalyzingSuggesterExtTest {

	public TRAnalyzingSuggesterExt suggester = null;

	public TRAnalyzingSuggesterExtTest() {
		doTest();
	}

	@Test
	public void doTest() {
		CharArraySet stopSet = new CharArraySet(CharArraySet.EMPTY_SET, false);

		Analyzer indexAnalyzer = new StandardAnalyzer(stopSet);
		Analyzer queryAnalyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

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

		try {

			PrepareDictionary dictionary = new PrepareDictionary(is,
					OrganizationEntry.class);

			suggester = new TRFuzzySuggesterExt(indexAnalyzer, queryAnalyzer);

			suggester.build(new EntryIterator(dictionary));

			dictionary.close();
			is.close();

		} catch (Exception e) {
			suggester = null;
		}

		assertNotNull(suggester);

		/***********************************
		 * List<LookupResult> allResults = null; try { allResults =
		 * suggester.lookup("ALBA IU", false, 5); } catch (IOException e) {
		 * allResults = null; }
		 * 
		 * SuggestData suggestData = new SuggestData();
		 * 
		 * for (LookupResult result : allResults) {
		 * 
		 * Map<String, String> map = PrepareDictionary.processJson(new String(
		 * ((TRAnalyzingSuggesterExt) suggester).getReturn(new String(
		 * result.payload.bytes), Process.json)));
		 * 
		 * Suggestions suggestions = suggestData.new Suggestions();
		 * suggestions.keyword = map.remove(Entry.TERM);
		 * suggestData.suggestions.add(suggestions);
		 * 
		 * Set<String> keys = map.keySet();
		 * 
		 * for (String key : keys) {
		 * 
		 * Info info = suggestData.new Info(); info.key = key; info.value =
		 * map.get(key); suggestions.info.add(info); } }
		 * 
		 * assertNotNull(suggestData);
		 * 
		 * String suggestion=suggestData.suggestions.get(0).keyword;
		 * 
		 * assertEquals("1 Decembrie 1918 University Alba Iulia",suggestion);
		 ***/

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
