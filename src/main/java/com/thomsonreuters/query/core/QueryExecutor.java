package com.thomsonreuters.query.core;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class QueryExecutor {
	
	private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class);
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	private QueryExecutor() {}
	
	public static List<Pair<String, String>> execute(List<Pair<String, String>> queries, String host) {
		
		List<Pair<String, String>> results = new ArrayList<>(); 
		FutureTask<Pair>[] workers = new FutureTask[queries.size()];

		for (int i = 0; i < queries.size(); i++) {
			String key = queries.get(i).getLeft();
			String queryToExecute = queries.get(i).getRight();
			workers[i] = (FutureTask<Pair>) executor.submit(
						new Callable<Pair>() {
							@Override
							public Pair call() throws Exception {
								try {
									String result = executeQuery(host,queryToExecute);
									return Pair.of(key, result);
								} catch (IOException e) {
									return Pair.of(key, null);
								}
							}}
						);
			executor.execute(workers[i]);

		}

		for (int i = 0; i < workers.length; i++) {
			try{
				results.add(workers[i].get(1000, TimeUnit.MILLISECONDS));
			}
			catch(Exception e){
				log.error("execute error: {}", e.getMessage(), e);
			}
		}
		
		return results;

	}
	
	private static String executeQuery(String urlString, String query) throws IOException {
		StringBuilder jsonBuffer = new StringBuilder();
		
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setRequestMethod("POST");
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setRequestProperty("Content-Type",
				"application/json; charset=UTF-8");

		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
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
			jsonBuffer.append("{\"took\":2,\"timed_out\":false,\"_shards\":{\"total\":5,\"successful\":5,\"failed\":0},\"hits\":{\"total\":0,\"max_score\":null,\"hits\":[]}}");
		}

		return jsonBuffer.toString();

	}
	
	

}
