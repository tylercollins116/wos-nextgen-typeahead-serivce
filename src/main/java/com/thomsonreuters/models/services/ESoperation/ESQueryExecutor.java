package com.thomsonreuters.models.services.ESoperation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

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
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

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

	private void execute(IQueryGenerator queryGenerator) {

		String result = "{}";

		try {

			long timeStart = System.currentTimeMillis();
			result = executeESQueryViaAppache(queryGenerator);

		} catch (Exception e) {
			e.printStackTrace();
			result = "{}";
		}

		queryGenerator.setResponse(result);

	}

	@Deprecated
	private String executeESQueryViaAppacheURLConnection(
			IQueryGenerator queryGenerator) throws Exception {
		StringBuilder jsonBuffer = new StringBuilder();

		/***************************************/

		String urlString = null;
		if (queryGenerator.getESURL() != null
				&& queryGenerator.getESURL().length() > 4) {
			urlString = queryGenerator.getESURL();
		} else {

			urlString = "http://" + PropertyValue.ELASTIC_SEARCH_URL
					+ PropertyValue.getProperty(queryGenerator.getSource())
					+ "/_search";
		}

		/***************************************/

		URL url = new URL(urlString);

		logger.debug("URL of ElasticSearch " + urlString);

		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setRequestProperty("Content-Type",
				"application/json; charset=UTF-8");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr,
				"UTF-8"));
		wr.write((Charset.forName("UTF-8").encode(queryGenerator.createQuery()))
				.array());
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
			e.printStackTrace();
		}

		return jsonBuffer.toString();
	}

	@Deprecated
	private String executeESQueryViaAppache(IQueryGenerator queryGenerator)
			throws Exception {

		StringBuilder jsonBuffer = new StringBuilder();

		String urlString = null;
		if (queryGenerator.getESURL() != null
				&& queryGenerator.getESURL().length() > 4) {
			urlString = queryGenerator.getESURL();
		} else {

			urlString = "http://" + PropertyValue.ELASTIC_SEARCH_URL
					+ PropertyValue.ES_SEARCH_PATH.get(queryGenerator.getSource())
					+ "/_search";
		}

		/***************************************/

		HttpPost gbPost = new HttpPost(urlString);

		logger.debug("URL of ElasticSearch " +urlString);

		HttpContext gbPostContext = new BasicHttpContext();

		ByteArrayEntity gbPostEntity = new ByteArrayEntity(queryGenerator
				.createQuery().getBytes("UTF-8"));

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

		this.execute(responseFormatter);

		SuggestData data = responseFormatter.formatResponse();

		return data;
	}

}
