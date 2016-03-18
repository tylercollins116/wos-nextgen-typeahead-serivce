package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thomsonreuters.models.SuggestData.Suggestions;

public class SuggestDataJsonTest {

	@Test
	public void checkJsonGeneration() {

		SuggestData suggestData = new SuggestData();
		suggestData.source = "wos";
		suggestData.took = "2";

		Suggestions suggestions = new SuggestData().new Suggestions();
		suggestions.keyword = "test";

		SuggestData.Info info1 = new SuggestData().new Info();
		info1.key = "count";
		info1.value = "0";

		suggestions.info.add(info1);
		suggestData.suggestions.add(suggestions);

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

		assertEquals(
				"{\"source\":\"wos\",\"took\":\"2\",\"suggestions\":[{\"keyword\":\"test\",\"info\":[{\"key\":\"count\",\"value\":\"0\"}]}]}",
				output.toString());
	}

}
