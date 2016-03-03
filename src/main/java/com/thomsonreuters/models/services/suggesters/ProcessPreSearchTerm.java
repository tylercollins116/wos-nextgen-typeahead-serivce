package com.thomsonreuters.models.services.suggesters;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
 
import com.thomsonreuters.client.statistics.StatisticsServiceClient;
import com.thomsonreuters.client.statistics.impl.StatisticsServiceClientImpl;
import com.thomsonreuters.models.services.suggesterOperation.IProcessPreSearchTerm;

public class ProcessPreSearchTerm implements IProcessPreSearchTerm {

	private final StatisticsServiceClient statisticsServiceClient = new StatisticsServiceClientImpl();

	@Override
	public String[] getPreSearchedTerm(String truid, String... info) {

		Calendar cal = Calendar.getInstance();
		Date endDate = cal.getTime();
		cal.add(Calendar.YEAR, -5); // to get previous year add -1
		Date startDate = cal.getTime();

		ByteBuf dataBytes = null;
		try {
			dataBytes = statisticsServiceClient
					.getStatisticsTerms("user", truid.trim(), "queries",
							startDate, endDate).observe().toBlocking()
					.lastOrDefault(Unpooled.copiedBuffer("".getBytes()));

			String statisticsResponse = dataBytes.toString(Charset
					.forName("UTF-8"));

			return formatResponse(truid, statisticsResponse);

		} catch (Exception e) {

		}
		return new String[] {};
		
		//return new String[]{"sgro","cancer","ski binding","gallagher","posts"};

	}

	private String[] formatResponse(String truid, String result) {

		List<String> terms = new ArrayList<String>();

		try {

			JSONObject sonObj = new JSONObject(result);

			JSONArray allTerms = sonObj.getJSONArray("terms");

			for (int seq_1 = 0; seq_1 < allTerms.length(); seq_1++) {

				Object obj_ = allTerms.get(seq_1);
				JSONObject finalObj = new JSONObject(obj_.toString());
				String fielddata = (finalObj.getString("term"));
				terms.add(fielddata);

			}
		} catch (Exception e) {
		}

		return terms.toArray(new String[] {});
	}

	@Override
	public String[] getSuggestions(String[] terms, String query) {

		final Map<String, String> tokens = new HashMap<String, String>();
		final List<String> processedToken = new ArrayList<String>();
		processPreSearchTerms(terms, processedToken, tokens);

		String[] results = match(processedToken, query, tokens);

		return results;
	}

	private void processPreSearchTerms(String[] allSearchedTokens,
			final List<String> processedToken, final Map<String, String> tokens) {

		for (String searchedTerm : allSearchedTokens) {
			String processed = processAndNormalizeToken(searchedTerm);
			tokens.put(processed, searchedTerm);
			processedToken.add(processed);
		}
		Collections.sort(processedToken);
	}

	@Override
	public String processAndNormalizeToken(String token) {

		StringBuilder filterCharacter = new StringBuilder();

		char[] filterChars = token.toCharArray();
		for (char c : filterChars) {
			if (c == '\'' || c == '"' || c == '?' || c == '*' || c == ','
					|| c == '[' || c == ']') {
				continue;
			}
			filterCharacter.append(Character.toLowerCase(c));
		}

		StringBuilder processString = new StringBuilder();
		StringTokenizer tokenizer = new StringTokenizer(
				filterCharacter.toString());

		while (tokenizer.hasMoreTokens()) {
			if (processString.length() == 0) {
				processString.append(tokenizer.nextToken());
			} else {
				processString.append("|" + tokenizer.nextToken());
			}
		}

		return processString.toString().trim();
	}

	private String[] match(final List<String> processedToken, String token,
			final Map<String, String> tokens) {

		int startIdx = binarySearch(processedToken, token, 0,
				processedToken.size() - 1);
		int endIdx = binarySearch(processedToken, token + '\uFFFF', 0,
				processedToken.size() - 1);

		ArrayList<String> objs = new ArrayList<String>(endIdx - startIdx);

		int length = endIdx - startIdx;
		for (int i = startIdx; i < endIdx; i++) {
			objs.add(tokens.get(processedToken.get(i)));

		}

		return objs.toArray(new String[] {});

	}

	/**
	 * Semi-standard binary search returning index of match location or where
	 * the location would match if it is not present.
	 */
	private int binarySearch(List<String> arr, String elem, int fromIndex,
			int toIndex) {
		int mid, cmp;
		while (fromIndex <= toIndex) {
			mid = (fromIndex + toIndex) / 2;
			if ((cmp = arr.get(mid).compareTo(elem)) < 0)
				fromIndex = mid + 1;
			else if (cmp > 0)
				toIndex = mid - 1;
			else
				return mid;
		}
		return fromIndex;
	}

	public static void main(String[] args) {}

}
