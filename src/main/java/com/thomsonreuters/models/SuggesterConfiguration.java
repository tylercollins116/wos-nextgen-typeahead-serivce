package com.thomsonreuters.models;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.lucene.search.suggest.analyzing.AnalyzingSuggester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.async.Job;
import com.thomsonreuters.models.services.async.NamedThreadFactory;
import com.thomsonreuters.models.services.async.WaitingBlockingQueue;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterFactory;
import com.thomsonreuters.models.services.suggesters.BlankSuggester;


@Singleton
public class SuggesterConfiguration implements SuggesterConfigurationHandler{

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterConfiguration.class);

	private DictionaryLoader<AnalyzingSuggester> dictionaryReader = null;

	private ExecutorService reloadExecutor;

	public SuggesterConfiguration() {

		try {

			reloadExecutor = new ThreadPoolExecutor(1, 6, 0L,
					TimeUnit.MICROSECONDS,
					new WaitingBlockingQueue<Runnable>(),
					new NamedThreadFactory("Suggester"));

			dictionaryReader = SuggesterFactory.createSuggesters("S3");

		} catch (Exception e) {
			e.printStackTrace();

			dictionaryReader = new BlankSuggester();
		}

		ConfigurationManager.getConfigInstance().addConfigurationListener(
				new ConfigurationListener() {

					@Override
					public void configurationChanged(ConfigurationEvent event) {

						log.info("reloding  dictionary "
								+ event.getPropertyName());

						Job<AnalyzingSuggester> job = new Job<AnalyzingSuggester>(
								dictionaryReader, event.getPropertyName());
						reloadExecutor.execute(job.inputTask);
						;

					}
				});
	}

	@Override
	public DictionaryLoader<AnalyzingSuggester> getDictionaryAnalyzer() {
		return this.dictionaryReader;
	}

}
