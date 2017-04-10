package com.thomsonreuters.models;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.suggesterOperation.models.company.TechnicalTypeaheadSuggester;

public class TechnicalTypeaheadTest {

	@Test
	public void testCompanyTypeahead() throws Exception {
		
		/*** creating and *********/
		InputStreamGenerator obj = new InputStreamGenerator();
		obj.addLine("{\"keyword\":\"CAPTURING RAW IMAGE\",\"count\":67,\"inf\":1.550053}");
		obj.addLine("{\"keyword\":\"AUTOMATICALLY DISCHARGING WATER\",\"count\":123,\"inf\":2.19788}");
		obj.addLine("{\"keyword\":\"SAMPLE LYMPH\",\"count\":1082,\"inf\":0.785297}");
		obj.addLine("{\"keyword\":\"MICROSTRIP TRANSMISSION LINE STRUCTURE\",\"count\":224,\"inf\":2.872266}");
		obj.addLine("{\"keyword\":\"INNATE AND ADAPTIVE IMMUNE RESPONSES\",\"count\":3451,\"inf\":0.681613}");
		obj.addLine("{\"keyword\":\"CYS GLY LEU\",\"count\":12295,\"inf\":0.643516}");
		obj.addLine("{\"keyword\":\"DESIRED SIGNAL TO INTERFERENCE\",\"count\":431,\"inf\":1.66702}");
		obj.addLine("{\"keyword\":\"FORMED CONE SHAPED\",\"count\":95,\"inf\":2.265757}");
		obj.addLine("{\"keyword\":\"NON INTRUSIVE APPLIANCE\",\"count\":113,\"inf\":1.940102}");
		obj.addLine("{\"keyword\":\"ANTI ELECTROSTATIC AGENT\",\"count\":134,\"inf\":0.954722}");
		obj.addLine("{\"keyword\":\"REMOTE COMPUTER CONNECTED\",\"count\":1189,\"inf\":1.031716}");
		obj.addLine("{\"keyword\":\"RETINOPATHY RETINOBLASTOMA RETROLENTAL\",\"count\":1265,\"inf\":0.771618}");
		obj.addLine("{\"keyword\":\"COMPLETE CHIP\",\"count\":1136,\"inf\":1.447263}");
		obj.addLine("{\"keyword\":\"NICKEL COBALT MOLYBDENUM CHROMIUM\",\"count\":4225,\"inf\":1.289782}");
		
		
		/******************************************/
		

		Long totalMemory = Runtime.getRuntime().totalMemory();
		
	
		
		long start = System.currentTimeMillis();
		
		System.out.println(new Date());

		final TechnicalTypeaheadSuggester suggester = new TechnicalTypeaheadSuggester(obj.getStream());
		
		
		System.out.println(new Date());
		
		System.out.println(System.currentTimeMillis()-start);


		
		SuggestData result = (suggester.lookup("INNAT", 2, 3,
				false));

		List<Suggestions> allSuggestions = result.suggestions;
		
		assertEquals("INNATE AND ADAPTIVE IMMUNE RESPONSES", result.suggestions.get(0).info.get(0).value);
		
		
		
		
		 

		 
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
