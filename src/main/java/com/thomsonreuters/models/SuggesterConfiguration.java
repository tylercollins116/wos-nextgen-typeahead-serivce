package com.thomsonreuters.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
import com.thomsonreuters.models.services.suggesters.BlankSuggester;
import com.thomsonreuters.models.services.util.ElasticEntityProperties;
import com.thomsonreuters.models.services.util.GroupTerms;
import com.thomsonreuters.models.services.util.Property;

@Singleton
public class SuggesterConfiguration implements SuggesterConfigurationHandler {

	private static final Logger log = LoggerFactory
			.getLogger(SuggesterConfiguration.class);

	private DictionaryLoader<Lookup> dictionaryReader = null;
	private HashMap<String, ElasticEntityProperties> elasticEntityProperties = new HashMap<>();

	private ExecutorService reloadExecutor;

	public SuggesterConfiguration() {

		try {

			reloadExecutor = new ThreadPoolExecutor(1, 6, 0L,
					TimeUnit.MICROSECONDS,
					new WaitingBlockingQueue<Runnable>(),
					new NamedThreadFactory("Suggester"));

			//dictionaryReader = SuggesterFactory.createSuggesters("S3");

			dictionaryReader = SuggesterFactory.createSuggesters("S3IAM");

		} catch (Exception e) {
			e.printStackTrace();

			dictionaryReader = new BlankSuggester();
		}

		// build ES URL
		prepareESURL();
		prepareESEntities();

		final Property property = new GroupTerms();
		
		
		ConfigurationManager.getConfigInstance().addConfigurationListener(
				new ConfigurationListener() {

					@Override
					public void configurationChanged(ConfigurationEvent event) {

						String triggredProperty = event.getPropertyName();
						log.info("********************************************");
						log.info("Executing Eiddo Configuration changed trigger for   "
								+ event.getPropertyName());

						if (property.isDictionaryRelated(triggredProperty)
								|| property.isBucketName(triggredProperty)) {

							log.info("reloding  dictionary "
									+ event.getPropertyName());

							Job<Lookup> job = new Job<Lookup>(dictionaryReader,
									event.getPropertyName());
							reloadExecutor.execute(job.inputTask);
						} else if (triggredProperty.trim().equalsIgnoreCase(
								Property.SEARCH_HOST)
								|| triggredProperty.trim().equalsIgnoreCase(
										Property.SEARCH_PORT)
								|| triggredProperty.trim().startsWith(
										Property.SEARCH_PATH_PREFIX)) {
							log.info("Preparing ES URL for  "
									+ event.getPropertyName());
							prepareESURL();
						} else if (triggredProperty.trim().startsWith(
								Property.ENTITY_PREFIX)) {
							log.info("Recreating ES entities for  "
									+ event.getPropertyName());
							prepareESEntities();

						}

					}
				});

		/** for eiddo change testing **/
		/**
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { try {
		 *           Thread.currentThread().sleep(1000*60*1); Job<Lookup> job =
		 *           new Job<Lookup>(dictionaryReader,
		 *           "dictionary.path.companyterms");
		 *           reloadExecutor.execute(job.inputTask); } catch
		 *           (InterruptedException e) { e.printStackTrace(); }
		 * 
		 *           } }).start();
		 **/
	}

	@Override
	public DictionaryLoader<Lookup> getDictionaryAnalyzer() {
		return this.dictionaryReader;
	}

	private void prepareESURL() {

		// String ELASTIC_SEARCH_URL =
		// ConfigurationManager.getConfigInstance().getString(Property.SEARCH_HOST)
		// + ":" +
		// ConfigurationManager.getConfigInstance().getString(Property.SEARCH_PORT);
		
		

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();
		
		
		log.info("*****************************************");
		log.info(" Starting process for Preparing ES URL ");
		
		HashMap<String, String> tmp_es_search_path = new HashMap<String, String>();

		while (keys.hasNext()) {
			String key = keys.next();

			if (key.startsWith(Property.SEARCH_PATH_PREFIX)) {

				String path = ConfigurationManager.getConfigInstance()
						.getString(key);
				key = key.toLowerCase().replace(
						Property.SEARCH_PATH_PREFIX + ".", "");
				tmp_es_search_path.put(key, path);

			}

		}

		Set<String> keyset = tmp_es_search_path.keySet();
		
		Property.ES_SEARCH_PATH.clear();
		for (String key : keyset) {
			Property.ES_SEARCH_PATH.put(key, tmp_es_search_path.get(key));
		}
		
		log.info(" Ending process for Preparing ES URL , Total number of entities URL  "
				+ Property.ES_SEARCH_PATH.size());
		log.info("*****************************************");

	}

	private Map<String, String> getKeyValueFields(String[] values) {

		Map<String, String> keyValueField = new LinkedHashMap<>();

		if (values.length > 0) {
			for (String value : values) {
				String[] keyValue = value.split(":");
				keyValueField.put(keyValue[0], keyValue[1]);
			}
		}
		return keyValueField;
	}

	private void prepareESEntities() {

		Map<String, ElasticEntityProperties> tmpElasticEntityProperties = new LinkedHashMap<>();
		log.info("*****************************************");
		Integer esVersion = ConfigurationManager.getConfigInstance().getInteger(Property.SEARCH_VERSION, 2);
		log.info(" Set ES version :"+esVersion);
		log.info("*****************************************");

		log.info("*****************************************");
		log.info(" Starting process for Preparing ES entities ");

		Iterator it = Property.ES_SEARCH_PATH.keySet().iterator();
		while (it.hasNext()) {
			String type = "";
			String[] searchField = null;
			String[] returnFields = null;
			Map<String, String> aliasFields = null;
			Map<String, String> sortFields = null;
			String analyzer = "";
			Integer[] maxExpansion = new Integer[] {};
			String slop = "3";
			String host = "";
			String port = "";
			String queryType = "prefix";

			String path = it.next().toString();
			ElasticEntityProperties eep = new ElasticEntityProperties();
			eep.setEsMainVersion(esVersion);
			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".type")) {
				type = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".type");
			}
			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".searchField")) {
				searchField = ConfigurationManager.getConfigInstance()
						.getStringArray(
								Property.ENTITY_PREFIX + path + ".searchField");
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".returnFields")) {
				returnFields = ConfigurationManager
						.getConfigInstance()
						.getStringArray(
								Property.ENTITY_PREFIX + path + ".returnFields");
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".aliasFields")) {
				aliasFields = getKeyValueFields(ConfigurationManager
						.getConfigInstance().getStringArray(
								Property.ENTITY_PREFIX + path + ".aliasFields"));
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".sortFields")) {
				sortFields = getKeyValueFields(ConfigurationManager
						.getConfigInstance().getStringArray(
								Property.ENTITY_PREFIX + path + ".sortFields"));
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".analyzer")) {
				analyzer = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".analyzer");
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".maxExpansion")) {
				maxExpansion = Stream
						.of(ConfigurationManager.getConfigInstance()
								.getStringArray(
										Property.ENTITY_PREFIX + path
												+ ".maxExpansion"))
						.map(Integer::parseInt).toArray(Integer[]::new);
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".port")) {
				port = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".port");
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".host")) {
				host = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".host");
			}

			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".slop")) {
				slop = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".slop");
			}
			if (ConfigurationManager.getConfigInstance().containsKey(
					Property.ENTITY_PREFIX + path + ".querytype")) {
				queryType = ConfigurationManager.getConfigInstance().getString(
						Property.ENTITY_PREFIX + path + ".querytype", "prefix");
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
			eep.setQueryType(queryType);
			tmpElasticEntityProperties.put(Property.ENTITY_PREFIX + path, eep);
		}

		if (elasticEntityProperties.size() > 0) {
			elasticEntityProperties.clear();
		}
		elasticEntityProperties = new HashMap<>(tmpElasticEntityProperties);
		log.info(" Ending process for Preparing ES entities , Total number of entities prepared  "
				+ elasticEntityProperties.size());
		log.info("*****************************************");

	}

	@Override
	public ElasticEntityProperties getElasticEntityProperties(String esPath) {
		return this.elasticEntityProperties.get(esPath);
	}

	@Override
	public Set<String> getRegisteredElasticEntityNames() {
		return this.elasticEntityProperties.keySet();
	}

}
