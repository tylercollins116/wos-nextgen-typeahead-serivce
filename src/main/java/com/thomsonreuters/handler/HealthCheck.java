package com.thomsonreuters.handler;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import netflix.karyon.health.HealthCheckHandler;

import org.apache.lucene.search.suggest.Lookup;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.annotations.Configuration;
import com.thomsonreuters.eiddo.client.EiddoClient;
import com.thomsonreuters.eiddo.client.EiddoListener;
import com.thomsonreuters.models.SuggestData;
import com.thomsonreuters.models.SuggesterConfigurationHandler;
import com.thomsonreuters.models.SuggesterHandler;
import com.thomsonreuters.models.services.suggesterOperation.IPA.IPASuggesterHandler;
import com.thomsonreuters.models.services.suggesterOperation.models.company.CompanyTypeaheadSuggester;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.GroupTerms;
import com.thomsonreuters.models.services.util.Property;

@Singleton
public class HealthCheck implements HealthCheckHandler {
	private static final Logger log = LoggerFactory
			.getLogger(HealthCheck.class);

	private final EiddoClient eiddo;
	private boolean eiddoCorrupted = false;

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;
	private final SuggesterHandler suggesterHandler;
	private final IPASuggesterHandler ipaSuggesterHandler;

	@Configuration(value = "search.host", documentation = "search host")
	private Supplier<String> host = Suppliers.ofInstance("localhost");

	@Configuration(value = "search.port", documentation = "search port")
	private Supplier<String> port = Suppliers.ofInstance("0");

	@Configuration(value = "search.path", documentation = "search path")
	private Supplier<String> path = Suppliers.ofInstance("/");

	@Inject
	public HealthCheck(EiddoClient eiddo,
			SuggesterConfigurationHandler suggesterConfigurationHandler,
			SuggesterHandler suggesterHandler,
			IPASuggesterHandler ipaSuggesterHandler) {

		this.eiddo = eiddo;
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;
		this.suggesterHandler = suggesterHandler;
		this.ipaSuggesterHandler = ipaSuggesterHandler;

		eiddo.addListener(new EiddoListener() {

			@Override
			public void onRepoChainUpdated(List<File> repoDir) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(Throwable error, boolean fatal) {
				if (fatal) {
					eiddoCorrupted = true;
				}

			}
		});
	}

	@PostConstruct
	public void init() {
		log.info("Health check initialized.");
	}

	@Override
	public int getStatus() {
		log.info("*************************************************");
		log.info("Health check called.");
		if (eiddoCorrupted) {
			log.error("Eiddo appears to be corrupted. The instance has to be terminated and relaunched");
			return 500;
		}

		try {

			log.info(String.format("AvaliableMemory %.3fGB",
					(Runtime.getRuntime().totalMemory())
							/ (1024.0 * 1024.0 * 1024.0)));
			log.info(String.format("UsedMemory %.3fGB", (Runtime.getRuntime()
					.totalMemory() - Runtime.getRuntime().freeMemory())
					/ (1024.0 * 1024.0 * 1024.0)));
			log.info(String.format("FreeMemory %.3fGB",
					(Runtime.getRuntime().freeMemory())
							/ (1024.0 * 1024.0 * 1024.0)));

		} catch (Exception e) {
			// No need to handle anything above code is just for information
		}

		if (checkLoadedDictionaryAndResults() == 200
				&& checkConnectionaAndResultsFromES() == 200) {
			return 200;
		} else {
			return 500;
		}

	}

	private int checkLoadedDictionaries() {

		Property property = new GroupTerms();

		/** check for all the dictionaries successfully loaded or not **/

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		String bucketName = null;
		Set<String> dictionaryProperties = new HashSet<String>();

		while (keys.hasNext()) {
			String key = keys.next();

			if (property.isBucketName(key)) {
				bucketName = ConfigurationManager.getConfigInstance()
						.getString(key);
			} else if (property.isDictionaryRelated(key)) {
				
				String dictionaryName=property.getDictionayName(key);
				if (dictionaryName!=null && !dictionaryName.endsWith("." + property.SUGGESTER)&&dictionaryName.split("\\.").length==1) {
					dictionaryProperties.add(dictionaryName);
				}
			}
		}

		// add one function in suggester.java//
		/**
		 * public DictionaryLoader<AnalyzingSuggester> getDictionaryReader() {
		 * return dictionaryReader; }
		 **/

		/** No more bucket check **/

		BlockingHashTable<String, Lookup> suggesters = (BlockingHashTable<String, Lookup>) suggesterConfigurationHandler
				.getDictionaryAnalyzer().getSuggesterList();

		Set<String> dictionaryNames = suggesters.keySet();

		boolean allSet = true;

		for (String dictionaryName : dictionaryProperties) {
			if ((!dictionaryNames.contains(dictionaryName))
					|| (suggesters.get(dictionaryName) == null)) {
				allSet = false;
			}
		}

		if (!allSet) {
			log.error("========= Available dictionary in eiddo===========\t:"
					+ dictionaryProperties.size());
			for (String name : dictionaryProperties) {
				log.error("\t\t" + name + "\t\t");
			}

			log.error("========= Available dictionary in applications===========\t:"
					+ dictionaryNames.size());
			Iterator<String> enums = dictionaryNames.iterator();
			while (enums.hasNext()) {
				log.error("\t\t" + enums.next() + "\t\t");
			}
			log.error("===========================================================");

			log.error("\tTotal number of loaded Dictionary Names mismatch with Eiddo Dictionary Names , this may be because the dictionary is still loading");

			return 500;
		}

		/** every thing seems good **/
		log.info("\tLoaded Dictionary Names : " + dictionaryNames);
		log.info("\tNo problem found in loaded Dictionaries");

		/**************************************************************/

		return 200;

	}

	private int checkLoadedDictionaryAndResults() {

		/**
		 * check for loaded dictionaries is removed because if a new dictionary
		 * is added into eiddo properties file and if it takes long time to load
		 * , then healthcheck may fail ...
		 */

		if (checkLoadedDictionaries() != 200) {
			// return 500;
		}

		BlockingHashTable<String, Lookup> suggesters = (BlockingHashTable<String, Lookup>) suggesterConfigurationHandler
				.getDictionaryAnalyzer().getSuggesterList();
		Set<String> dictionaryNames = suggesters.keySet();

		log.info("\tSuccessfully loaded dictionaries are   " + dictionaryNames);

		boolean allSet = true;

		for (String dictionaryName : dictionaryNames) {
			if (suggesters.get(dictionaryName) instanceof CompanyTypeaheadSuggester) {

				String result = ipaSuggesterHandler.lookup(dictionaryName, "c",
						2, false, false);

				JSONObject Json = null;
				JSONArray array = null;
				try {
					Json = new JSONObject(result);

					array = Json.getJSONArray("suggestion");

				} catch (Exception e) {
					// no need to log anything here .. result will be evaluated
					// on later code
				}

				if (array == null || array.length() < 2) {
					allSet = false;

					log.error("Doesn't seems like typeahead service  "
							+ dictionaryName
							+ " which execute against Dictionary is working properly .. Need attention !!! .Output doesnt match the desired result ");
				} else {
					log.info("\ttypeahead service  "
							+ dictionaryName
							+ "which execute against dictionary is working fine ");
				}

			} else {
				List<SuggestData> results = suggesterHandler.lookup(
						dictionaryName, "a", 2);
				if (results.size() <= 0
						|| results.get(0).suggestions.size() < 2) {

					allSet = false;

					log.error("Doesn't seems like typeahead service  "
							+ dictionaryName
							+ "which execute against Dictionary is working properly .. Need attention !!! .Output doesnt match the desired result ");

				} else {
					log.info("\ttypeahead service  "
							+ dictionaryName
							+ " which execute against dictionary is working fine ");
				}
			}

		}

		if (!allSet) {
			return 500;
		}

		return 200;
	}

	private int checkESConnection() {

		/**
		 * check if it require to test ES connection or not. In some case
		 * micro-services doesn't use ES and only use dictionary so it no mean
		 * to check connection with ES for this condition.
		 */

		if ((host.get() == null || host.get().equalsIgnoreCase("localhost"))
				&& (port.get() == null || port.get().equalsIgnoreCase("0"))) {

			// doesn't use ES time to

			log.info("\tDoesn't find information about ES ..  So this service doesn't need to test  ES connection ");
			return 200;

		}

		/**
		 * End
		 */

		boolean success = false;

		InetAddress[] hostAddresses;
		try {
			hostAddresses = InetAddress.getAllByName(host.get());
		} catch (UnknownHostException e) {
			log.warn("Unable to lookup hosts for {}", host.get());
			return 500;
		}

		// for each address returned by the dns lookup, perform a HEAD request
		// against
		// the configured path to verify that ES is up AND that the configured
		// path is accessible
		// a http timeout or a non-200 response indicates an error on the given
		// host address.
		// at least one of the host addresses must be up and running for a
		// health check to be successful
		for (InetAddress hostAddress : hostAddresses) {
			URL url;
			try {
				url = new URL(String.format("http://%s:%s%s",
						hostAddress.getCanonicalHostName(), port.get(),
						path.get()));
			} catch (MalformedURLException e) {
				log.warn(
						"Unable to generate a valid url for host: {} with host address: {}",
						host.get(), hostAddress.getCanonicalHostName());

				// stop processing this host address, it is bad
				continue;
			}

			String hostUrl = url.toExternalForm();
			HttpURLConnection connection;
			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(2000);
				connection.setReadTimeout(2000);
				connection.setRequestMethod("HEAD");

				int responseCode = connection.getResponseCode();
				if (responseCode != 200) {
					log.warn("{} test failed.  Response: {}", hostUrl,
							responseCode);
					continue;
				} else {
					success = true;
					if (log.isDebugEnabled()) {
						log.debug("{} passed", hostUrl);
					}
				}
			} catch (IOException e) {
				log.warn(
						"Error connecting to host: {} with host address: {}: {}",
						host.get(), url.getHost(), e.getMessage());
			}
		}

		// if success flag was set by any of the host addresses return a
		// successful health check, otherwise error out
		if (!success) {
			log.warn("Health Check Failed, no valid host addresses are up");
			return 500;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Health Check Successful, at least one host address is up");
			}

			return 200;
		}
	}

	public int checkConnectionaAndResultsFromES() {

		if (checkESConnection() != 200) {
			return 500;
		}

		Set<String> registerKeys = suggesterConfigurationHandler
				.getRegisteredElasticEntityNames();

		boolean allSet = true;
		for (String key : registerKeys) {

			if (key.startsWith(Property.ENTITY_PREFIX)) {
				key = key.replace(Property.ENTITY_PREFIX, "").trim();
			}

			List<SuggestData> results = suggesterHandler.lookup(key, "c", 2);

			if ((results == null || results.size() < 1)
					|| (results.get(0) == null || results.get(0).suggestions
							.size() < 2)) {

				allSet = false;

				log.error("Doesn't seems like typeahead service  "
						+ key
						+ " which execute against ES is working properly .. Need attention !!! .Output doesnt match the desired result ");

			} else {
				log.info("\ttypeahead service  " + key
						+ " which execute query againse ES is  working fine ");
			}

		}

		if (!allSet) {
			return 500;
		}

		return 200;
	}
}
