package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomsonreuters.models.SuggestData.Suggestions;

public class SuggestDataJsonTest {

	@Test
	public void checkJsonGeneration() {

		SuggestData suggestData = new SuggestData();
		suggestData.source = "wos";
		suggestData.took = "2";

		/**--------------------------------------------------------**/
		Suggestions suggestions1 = new SuggestData().new Suggestions();
		suggestions1.keyword = "test1";

		SuggestData.Info info1 = new SuggestData().new Info();
		info1.key = "count";
		info1.value = "10";

		suggestions1.info.add(info1);
		suggestData.suggestions.add(suggestions1);
		
		/**--------------------------------------------------------**/
		Suggestions suggestions2 = new SuggestData().new Suggestions();
		suggestions2.keyword = "test2";

		SuggestData.Info info2 = new SuggestData().new Info();
		info2.key = "count";
		info2.value = "20";

		suggestions2.info.add(info2);
		suggestData.suggestions.add(suggestions2);
		/**--------------------------------------------------------**/

		ObjectMapper mapper = new ObjectMapper();
		OutputStream output = null;
		try {

			output = new OutputStream() {
				private StringBuilder string = new StringBuilder();

				@Override
				public void write(int x) throws IOException {
					this.string.append((char) x);
				}

				public String toString() {
					return this.string.toString();
				}
			};

			mapper.writeValue(output, suggestData);

		} catch (Exception e) {
			output = null;
		}

		assertNotNull(output);
		
		System.out.println(output.toString());

		assertEquals(
				"{\"source\":\"wos\",\"took\":\"2\",\"suggestions\":[{\"keyword\":\"test1\",\"info\":[{\"key\":\"count\",\"value\":\"10\"}]},{\"keyword\":\"test2\",\"info\":[{\"key\":\"count\",\"value\":\"20\"}]}]}",
				output.toString());
		
		Collections.sort(suggestData.suggestions,new Comparator<Suggestions>() {

			@Override
			public int compare(Suggestions o1, Suggestions o2) {
				return (o2.countToSort().compareTo(o1.countToSort()));
			}
		});
		
		assertEquals(suggestions2, suggestData.suggestions.get(0));
	}

}
