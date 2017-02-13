package com.thomsonreuters.models.services.ESoperation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggestData.Info;
import com.thomsonreuters.models.SuggestData.Suggestions;
import com.thomsonreuters.models.services.util.Property;

@Singleton
public class ESQueryExecutor implements IESQueryExecutor {

	private static final Logger logger = LoggerFactory
			.getLogger(ESQueryExecutor.class);

	private HttpClient httpClient;

	// String elasticURL =
	// "http://1p-es-dev.int.thomsonreuters.com:9200/wos20/_search";

	@Inject
	public ESQueryExecutor() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(50);
		httpClient = HttpClients.createMinimal(cm);

	}

	private void executeQuery(IQueryGenerator queryGenerator,
			List<String> results) {

		try {

			long timeStart = System.currentTimeMillis();
			collectResultsFromES(queryGenerator, results);

		} catch (Exception e) {
			e.printStackTrace();

		}

	}

	// =================================================================
	private void collectResultsFromES(IQueryGenerator queryGenerator,
			final List<String> results) throws Exception {

		String[] queries = queryGenerator.createQuery();
		logQueries(queries);
		ExecutorService executor = Executors.newFixedThreadPool(queries.length);

		FutureTask<String>[] workers = new FutureTask[queries.length];

		for (int i = 0; i < queries.length; i++) {

			workers[i] = new FutureTask<String>(new collectorCallable(
					queryGenerator, queries[i]));

			executor.execute(workers[i]);
		}

		for (int i = 0; i < workers.length; i++) {
			try{
			results.add(workers[i].get(1000, TimeUnit.MILLISECONDS));
			}catch(Exception e){}
		}
		executor.shutdown();

	}

	private void logQueries(String[] queries) {
		if (queries == null || queries.length == 0) {
			return;
		}
		logger.info("Number of ES queries generated : " + queries.length);
		for (int i = 0; i < queries.length; i++) {
			logger.info("Query : " + i);
			logger.info(queries[i]);
		}
	}

	public class collectorCallable implements Callable<String> {

		private final IQueryGenerator queryGenerator;
		private final String query;

		public collectorCallable(IQueryGenerator queryGenerator, String query) {
			this.queryGenerator = queryGenerator;
			this.query = query;
		}

		@Override
		public String call() throws Exception {
			return executeESQueryViaAppacheURLConnection(queryGenerator,
					this.query);
		}

	}

	// =================================================================

	@Deprecated
	private String executeESQueryViaAppacheURLConnection(
			IQueryGenerator queryGenerator, String query) throws Exception {
		StringBuilder jsonBuffer = new StringBuilder();

		/***************************************/

		String urlString = null;
		if (queryGenerator.getESURL() != null
				&& queryGenerator.getESURL().length() > 4) {
			urlString = queryGenerator.getESURL();
		} else {

			String esurl = ConfigurationManager.getConfigInstance().getString(
					Property.SEARCH_HOST);
			String port = ConfigurationManager.getConfigInstance().getString(
					Property.SEARCH_PORT);

			urlString = "http://" + esurl + ":" + port + "/"
					+ Property.ES_SEARCH_PATH.get(queryGenerator.getSource())
					+ "/_search";
		}

		/***************************************/

		URL url = new URL(urlString);

		// logger.info("URL of ElasticSearch " + urlString);
		// System.out.println("URL of ElasticSearch " + urlString);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setRequestProperty("Content-Type",
				"application/json; charset=UTF-8");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr,
				"UTF-8"));
		wr.write((Charset.forName("UTF-8").encode(query)).array());
		wr.flush();
		wr.close();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));

			String responseData = "";

			while ((responseData = br.readLine()) != null) {
				jsonBuffer.append(responseData);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			jsonBuffer.append("{\"took\":2,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":0,\"max_score\":null,\"hits\":[]}}");
		}

		return jsonBuffer.toString();
	}

	@Deprecated
	private String executeESQueryViaAppache(IQueryGenerator queryGenerator,
			String query) throws Exception {

		StringBuilder jsonBuffer = new StringBuilder();

		String urlString = null;
		if (queryGenerator.getESURL() != null
				&& queryGenerator.getESURL().length() > 4) {
			urlString = queryGenerator.getESURL();
		} else {

			String esurl = ConfigurationManager.getConfigInstance().getString(
					Property.SEARCH_HOST);
			String port = ConfigurationManager.getConfigInstance().getString(
					Property.SEARCH_PORT);

			urlString = "http://" + esurl + ":" + port
					+ Property.ES_SEARCH_PATH.get(queryGenerator.getSource())
					+ "/_search";
		}

		/***************************************/

		HttpPost gbPost = new HttpPost(urlString);

		// logger.info("URL of ElasticSearch " +urlString);
		// System.out.println("URL of ElasticSearch " + urlString);

		HttpContext gbPostContext = new BasicHttpContext();

		ByteArrayEntity gbPostEntity = new ByteArrayEntity(
				query.getBytes("UTF-8"));

		gbPost.setEntity(gbPostEntity);

		HttpResponse gbPostResponse = null;

		try {
			gbPostResponse = httpClient.execute(gbPost, gbPostContext);
		} catch (Exception e) {
			gbPostResponse = httpClient.execute(gbPost, gbPostContext);
		}

		HttpEntity gbPostResponseEntity = gbPostResponse.getEntity();
		BufferedReader htmlReader = new BufferedReader(new InputStreamReader(
				gbPostResponseEntity.getContent(), "UTF-8"));

		String line;

		while ((line = htmlReader.readLine()) != null) {
			jsonBuffer.append(line);
		}
		htmlReader.close();

		return jsonBuffer.toString();
	}

	public SuggestData formatResult(IQueryGenerator responseFormatter)
			throws Exception {
		List<String> results = new ArrayList<String>();
		this.executeQuery(responseFormatter, results);
		SuggestData[] data = new SuggestData[results.size()];

		for (int i = 0; i < results.size(); i++) {
			responseFormatter.setResponse(results.get(i));
			data[i] = responseFormatter.formatResponse();
		}
		return mergeFinalResult(data);
	}

	private SuggestData mergeFinalResult(SuggestData[] data) {
		if (data == null || data.length == 0) {
			return new SuggestData();
		} else if (data.length == 1) {
			return data[0];
		} else {

			Set<String> uniqueTerms = new HashSet<String>();
			SuggestData first = null;

			int firstIndex = -1;
			;
			for (; firstIndex < data.length;) {
				++firstIndex;
				if (first == null) {
					first = data[firstIndex];
				}
				if (first != null) {
					break;
				}
			}

			if (first != null) {
				for (Suggestions suggestion : first.suggestions) {
					List<Info> infos = suggestion.info;
					for (Info term : infos) {
						/**
						 * term_string is important in here its necessary to do
						 * hardcoding in here
						 **/
						if (term.key.equalsIgnoreCase("term_string")) {
							uniqueTerms.add(term.value);
						}
					}
				}

				for (int i = firstIndex + 1; i < data.length; i++) {

					for (Suggestions suggestion : data[i].suggestions) {

						List<Info> infos = suggestion.info;
						for (Info term : infos) {
							/**
							 * term_string is important in here its necessary to
							 * do hardcoding in here
							 **/
							if (term.key.equalsIgnoreCase("term_string")) {
								if (!uniqueTerms.contains(term.value)) {
									first.suggestions.add(suggestion);
									uniqueTerms.add(term.value);
								}

							}
						}

					}

				}

			} else {
				first = new SuggestData();
			}

			return first;
		}
	}
}
