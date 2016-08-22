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

import com.thomsonreuters.models.services.suggesterOperation.ext.TRAnalyzingSuggester;
import com.thomsonreuters.models.services.suggesterOperation.ext.TRFuzzySuggester;
import com.thomsonreuters.models.services.suggesterOperation.models.EntryIterator;
import com.thomsonreuters.models.services.suggesterOperation.models.OrganizationEntry;
import com.thomsonreuters.models.services.util.PrepareDictionary;

public class TRAnalyzingSuggesterTest {

	TRAnalyzingSuggester suggester = null;

	public TRAnalyzingSuggesterTest() {
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
					new OrganizationEntry());

			suggester = new TRFuzzySuggester(indexAnalyzer, queryAnalyzer);

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
