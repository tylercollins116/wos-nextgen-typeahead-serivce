package com.thomsonreuters.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;

import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.async.Job;
import com.thomsonreuters.models.services.async.NamedThreadFactory;
import com.thomsonreuters.models.services.async.WaitingBlockingQueue;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterFactory;
import com.thomsonreuters.models.services.suggesters.BlankSuggester;

public class Suggester {

	private static Suggester instance;

	private DictionaryLoader<AnalyzingSuggester> dictionaryReader = null;

	private ExecutorService reloadExecutor;

	private Suggester() {

		try {

			reloadExecutor = new ThreadPoolExecutor(1, 6, 0L,
					TimeUnit.MICROSECONDS, new WaitingBlockingQueue<Runnable>(),
					new NamedThreadFactory("Suggester"));

			dictionaryReader = SuggesterFactory.createSuggesters("S3");
			 
		} catch (Exception e) {
			e.printStackTrace();

			dictionaryReader = new BlankSuggester();
		}

			ConfigurationManager.getConfigInstance().addConfigurationListener(
					new ConfigurationListener() {

						@Override
						public void configurationChanged(
								ConfigurationEvent event) {

							Job<AnalyzingSuggester> job = new Job<AnalyzingSuggester>(
									dictionaryReader, event.getPropertyName());
							reloadExecutor.execute(job.inputTask);
							;

						}
					});

		
	}

	public static Suggester getInstance() {
		if (instance == null) {

			synchronized (Suggester.class) {

				if (instance == null) {

					instance = new Suggester();
				}

			}

		}
		return instance;

	}

	public static List<SuggestData> lookup(String query, int n) {

		return lookup("default", query, n);
	}

	public static List<SuggestData> lookup(String path, String query, int n) {
		List<SuggestData> results = new ArrayList<SuggestData>();

		AnalyzingSuggester suggester = Suggester.getInstance().dictionaryReader
				.getSuggesterList().get(path);

		try {
			for (LookupResult result : suggester.lookup(query, false, n)) {
				results.add(new SuggestData(result.key.toString()));
			}
		} catch (IOException e) {			 
		}

		return results;
	}

	public static void main(String[] args) throws IOException {

		Suggester object = Suggester.getInstance();
		List<SuggestData> results = object.lookup("default", "chi", 10);

		for (SuggestData data : results) {
			System.out.println(data.getValue());
		}

	}

}
