package com.thomsonreuters.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.thomsonreuters.models.services.suggesterOperation.models.ArticleEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.CategoryEntry;
import com.thomsonreuters.models.services.suggesterOperation.models.Entry;
import com.thomsonreuters.models.services.suggesterOperation.models.KeywordEntry; 
import com.thomsonreuters.models.services.util.PrepareDictionary;

@RunWith(value = Parameterized.class)
public class EntryTest {

	String value = "{\"count\":\"5\"}";

	private String value1;
	private String value2;
	private String value3;
	private String parameter;

	public EntryTest(String value1, String value2, String parameter) {
		this.value1 = value1;
		this.value2 = value2;
		this.parameter = parameter;
	}

	@Parameters
	public static Iterable<String[]> getValues() {

		return Arrays
				.asList(new String[][] {
						{
								"{\"count\":\"5\"}",
								"{\"keyword\":\"undulation and bio composite\",\"count\":5}",
								"1" },
						{
								"{\"count\":\"11\",\"fuid\":\"472494968WOS1\",\"keyword\":\"adsorption\",\"UT\":\"WOS:000337985600052\"}",
								"{\"keyword\":\"adsorption\",\"count\":11,\"UT\":\"WOS:000337985600052\",\"title\":\"Adsorption behavior and mechanism of perfluorinated compounds on various adsorbents-A review\",\"fuid\":\"472494968WOS1\"}",
								"2" },
						{
								"{\"count\":\"16\",\"category\":\"PHYSIOLOGY\"}",
								"{\"keyword\":\"pulmonary endothelial dysfunction\",\"category\":\"PHYSIOLOGY\",\"count\":16}",
								"3" },
						{
								"{\"institution\":\"NOAA\",\"country\":\"USA\",\"role\":\"Chaplain\",\"count\":\"1\",\"id\":\"ee3faaf5-1486-4cb5-9a29-88468106e1f8\"}",
								"{\"keyword\":\"Ginoux, A Paul\",\"count\":1,\"id\":\"ee3faaf5-1486-4cb5-9a29-88468106e1f8\",\"institution\":\"NOAA\",\"image\":\"http://example.com/imageName.jpg\",\"country\":\"USA\",\"role\":\"Chaplain\"}",
								"4" },
						{
								"{\"patentno\":\"US20150291212A1\"}",
								"{\"keyword\":\"ARTICULATED MOTOR VEHICLE\",\"patentno\":\"US20150291212A1\"}",
								"5" }

				});

	}

	@Test
	public void test() {

		Map<String, String> JsonToMap = PrepareDictionary.processJson(value2);
		Entry entry = null;

		if (parameter.equals("1")) {

			entry = new KeywordEntry(JsonToMap);

		} else if (parameter.equals("2")) {

			entry = new ArticleEntry(JsonToMap);

		} else if (parameter.equals("3")) {

			entry = new CategoryEntry(JsonToMap);

		}  

		assertNotNull(entry);

		value3 = entry.getJson();
		assertEquals(value1, value3);

	}

}
