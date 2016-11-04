package com.thomsonreuters.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
import com.thomsonreuters.models.services.util.ElasticEntityProperties;
import com.thomsonreuters.models.services.util.GroupTerms;
import com.thomsonreuters.models.services.util.Property;

@Singleton
public class SuggesterConfiguration implements SuggesterConfigurationHandler {

	private static final Logger log = LoggerFactory.getLogger(SuggesterConfiguration.class);

	private DictionaryLoader<Lookup> dictionaryReader = null;
	private HashMap<String, ElasticEntityProperties> elasticEntityProperties = new HashMap<>();

	private ExecutorService reloadExecutor;

	public SuggesterConfiguration() {

		try {

			reloadExecutor = new ThreadPoolExecutor(1, 6, 0L, TimeUnit.MICROSECONDS,
					new WaitingBlockingQueue<Runnable>(), new NamedThreadFactory("Suggester"));

			//dictionaryReader = SuggesterFactory.createSuggesters("S3");

			dictionaryReader = SuggesterFactory.createSuggesters("S3IAM");

		} catch (Exception e) {
			e.printStackTrace();

			dictionaryReader = new BlankSuggester();
		}

		// build ES URL
		prepareESURL();
		prepareESEntities();
		
		final Property property=new GroupTerms();

		ConfigurationManager.getConfigInstance().addConfigurationListener(new ConfigurationListener() {

			@Override
			public void configurationChanged(ConfigurationEvent event) {

				String triggredProperty = event.getPropertyName();

				if (property.isDictionaryRelated(triggredProperty)
						|| property.isBucketName(triggredProperty)) {

					log.info("reloding  dictionary " + event.getPropertyName());

					Job<Lookup> job = new Job<Lookup>(dictionaryReader, event.getPropertyName());
					reloadExecutor.execute(job.inputTask);
				} else if (triggredProperty.trim().equalsIgnoreCase(Property.SEARCH_HOST)
						|| triggredProperty.trim().equalsIgnoreCase(Property.SEARCH_PORT)
						|| triggredProperty.trim().startsWith(Property.SEARCH_PATH_PREFIX)) {

					prepareESURL();
				} else if (triggredProperty.trim().startsWith("entity.")) {

					prepareESEntities();

				} 

			}
		});
	}
              
	@Override
	public DictionaryLoader<Lookup> getDictionaryAnalyzer() {
		return this.dictionaryReader;
	}

	private void prepareESURL() {

//		String ELASTIC_SEARCH_URL = ConfigurationManager.getConfigInstance().getString(Property.SEARCH_HOST)
//				+ ":" + ConfigurationManager.getConfigInstance().getString(Property.SEARCH_PORT);

		Iterator<String> keys = ConfigurationManager.getConfigInstance().getKeys();

		while (keys.hasNext()) {
			String key = keys.next();

			if (key.startsWith(Property.SEARCH_PATH_PREFIX)) {

				String path = ConfigurationManager.getConfigInstance().getString(key);
				key = key.toLowerCase().replace(Property.SEARCH_PATH_PREFIX + ".", "");
				Property.ES_SEARCH_PATH.put(key, path);

			}

		}

	}

	private HashMap<String, String> getKeyValueFields(String[] values) {

		HashMap<String, String> keyValueField = new HashMap<String, String>();

		if (values.length > 0) {
			for (String value : values) {
				String[] keyValue = value.split(":");
				keyValueField.put(keyValue[0], keyValue[1]);
			}
		}
		return keyValueField;
	}

	private void prepareESEntities() {
	 
		Iterator it = Property.ES_SEARCH_PATH.keySet().iterator();
		while (it.hasNext()) {
			String type = "";
			String[] searchField = null;
			String[] returnFields = null;
			HashMap<String, String> aliasFields = null;
			HashMap<String, String> sortFields = null;
			String analyzer = "";
			Integer[] maxExpansion = new Integer[] {};
			String slop="3";
			String host="";
			String port="";

			String path = it.next().toString();
			ElasticEntityProperties eep = new ElasticEntityProperties();
			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".type")) {
				type = ConfigurationManager.getConfigInstance().getString("entity." + path + ".type");
			}
			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".searchField")) {
				searchField = ConfigurationManager.getConfigInstance().getStringArray("entity." + path + ".searchField");
			}

			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".returnFields")) {
				returnFields = ConfigurationManager.getConfigInstance()
						.getStringArray("entity." + path + ".returnFields");
			}

			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".aliasFields")) {
				aliasFields = getKeyValueFields(
						ConfigurationManager.getConfigInstance().getStringArray("entity." + path + ".aliasFields"));
			}

			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".sortFields")) {
				sortFields = getKeyValueFields(
						ConfigurationManager.getConfigInstance().getStringArray("entity." + path + ".sortFields"));
			}

			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".analyzer")) {
				analyzer = ConfigurationManager.getConfigInstance().getString("entity." + path + ".analyzer");
			}

			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".maxExpansion")) {
				maxExpansion = Stream
						.of(ConfigurationManager.getConfigInstance().getStringArray("entity." + path + ".maxExpansion"))
						.map(Integer::parseInt).toArray(Integer[]::new);
			}
			
			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".port")) {
				port = ConfigurationManager.getConfigInstance().getString("entity." + path + ".port");
			}
			
			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".host")) {
				host = ConfigurationManager.getConfigInstance().getString("entity." + path + ".host");
			}
			
			if (ConfigurationManager.getConfigInstance().containsKey("entity." + path + ".slop")) {
				slop = ConfigurationManager.getConfigInstance().getString("entity." + path + ".slop");
			}

			eep.setType(type);
			eep.setSearchField(searchField);
			eep.setReturnFields(returnFields);
			eep.setAliasFields(aliasFields);
			eep.setSortFields(sortFields);
			eep.setAnalyzer(analyzer);
			eep.setMaxExpansion(maxExpansion);
			eep.setSlop(slop);
			eep.setHost(host);
			eep.setPort(port);
			elasticEntityProperties.put("entity." + path, eep);
		}

	}

	@Override
	public ElasticEntityProperties getElasticEntityProperties(String esPath) {
		return this.elasticEntityProperties.get(esPath);
	}

}
