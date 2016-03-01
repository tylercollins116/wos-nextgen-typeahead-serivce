package com.thomsonreuters.models;

import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.lucene.search.suggest.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.thomsonreuters.models.services.async.Job;
import com.thomsonreuters.models.services.async.NamedThreadFactory;
import com.thomsonreuters.models.services.async.WaitingBlockingQueue;
import com.thomsonreuters.models.services.suggesterOperation.DictionaryLoader;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterFactory;
import com.thomsonreuters.models.services.suggesterOperation.SuggesterHelper;
import com.thomsonreuters.models.services.suggesters.BlankSuggester;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class SuggesterConfiguration implements SuggesterConfigurationHandler {

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterConfiguration.class);

	private DictionaryLoader<Lookup> dictionaryReader = null;

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

		// build ES URL
		prepareESURL();

		ConfigurationManager.getConfigInstance().addConfigurationListener(
				new ConfigurationListener() {

					@Override
					public void configurationChanged(ConfigurationEvent event) {

						String triggredProperty = event.getPropertyName();

						if (PropertyValue.getProperty(triggredProperty)
								.isDictionaryPathRelated()
								|| PropertyValue.getProperty(triggredProperty)
										.isBucketName()) {

							log.info("reloding  dictionary "
									+ event.getPropertyName());

							Job<Lookup> job = new Job<Lookup>(dictionaryReader,
									event.getPropertyName());
							reloadExecutor.execute(job.inputTask);
						} else if (triggredProperty.trim().equalsIgnoreCase(
								Property.DEFAULT_TYPEAHEAD_TYPES)) {

							String[] typeaheadvalues = ConfigurationManager
									.getConfigInstance().getStringArray(
											triggredProperty);

							if (typeaheadvalues != null
									&& typeaheadvalues.length > 0) {
								PropertyValue.SELECTED_DEFAULT_TYPEAHEADS = typeaheadvalues;
							}

						} else if (triggredProperty.trim().equalsIgnoreCase(
								Property.SEARCH_HOST)
								|| triggredProperty.trim().equalsIgnoreCase(
										Property.SEARCH_PORT)
								|| triggredProperty.trim().startsWith(
										Property.SEARCH_PATH_PREFIX)) {

							prepareESURL();

						} else {
							SuggesterHelper
									.loadFuzzynessThreshold(triggredProperty);
						}

					}
				});
	}

	@Override
	public DictionaryLoader<Lookup> getDictionaryAnalyzer() {
		return this.dictionaryReader;
	}

	private void prepareESURL() {

		PropertyValue.ELASTIC_SEARCH_URL = ConfigurationManager
				.getConfigInstance().getString(Property.SEARCH_HOST)
				+ ":"
				+ ConfigurationManager.getConfigInstance().getString(
						Property.SEARCH_PORT);

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		while (keys.hasNext()) {
			String key = keys.next();

			if (key.startsWith(Property.SEARCH_PATH_PREFIX)) {

				String path = ConfigurationManager.getConfigInstance()
						.getString(key);
				key = key.toLowerCase().replace(
						Property.SEARCH_PATH_PREFIX + ".", "");
				Property.ES_SEARCH_PATH.put(key, path);

			}

		}

	}

}
