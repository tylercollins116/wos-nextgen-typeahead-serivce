package com.thomsonreuters.handler;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import netflix.karyon.health.HealthCheckHandler;

import org.apache.lucene.search.suggest.Lookup;
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
import com.thomsonreuters.models.SuggesterConfigurationHandler;
import com.thomsonreuters.models.services.util.BlockingHashTable;
import com.thomsonreuters.models.services.util.Property;
import com.thomsonreuters.models.services.util.PropertyValue;

@Singleton
public class HealthCheck implements HealthCheckHandler {
	private static final Logger log = LoggerFactory
			.getLogger(HealthCheck.class);

	private final EiddoClient eiddo;
	private boolean eiddoCorrupted = false;

	private final SuggesterConfigurationHandler suggesterConfigurationHandler;

	@Configuration(value = "search.host", documentation = "search host")
	private Supplier<String> host = Suppliers.ofInstance("localhost");

	@Configuration(value = "search.port", documentation = "search port")
	private Supplier<String> port = Suppliers.ofInstance("9200");

	@Configuration(value = "search.path", documentation = "search path")
	private Supplier<String> path = Suppliers.ofInstance("/");

	@Inject
	public HealthCheck(EiddoClient eiddo,
			SuggesterConfigurationHandler suggesterConfigurationHandler) {

		this.eiddo = eiddo;
		this.suggesterConfigurationHandler = suggesterConfigurationHandler;

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
		log.info("Health check called.");
		if (eiddoCorrupted) {
			log.error("Eiddo appears to be corrupted. The instance has to be terminated and relaunched");
			return 500;
		}

		if (checkLoadedDictionaries() == 200 && checkESConnection() == 200) {
			return 200;
		} else {
			return 500;
		}

	}

	private int checkLoadedDictionaries() {

		/** check for all the dictionaries successfully loaded or not **/

		Iterator<String> keys = ConfigurationManager.getConfigInstance()
				.getKeys();

		String bucketName = null;
		List<String> dictionaryProperties = new ArrayList<String>();

		while (keys.hasNext()) {
			String key = keys.next();

			Property property = PropertyValue.getProperty(key);

			if (property.isBucketName()) {
				bucketName = ConfigurationManager.getConfigInstance()
						.getString(key);
			} else if (property.isDictionaryPathRelated()) {
				dictionaryProperties.add(property.getDictionayName());
			}
		}

		// add one function in suggester.java//
		/**
		 * public DictionaryLoader<AnalyzingSuggester> getDictionaryReader() {
		 * return dictionaryReader; }
		 **/

		if (bucketName == null) {
			log.error("Bucket Name Empty, returning error code : 500");

			return 500;
		}

		BlockingHashTable<String, Lookup> suggesters = (BlockingHashTable<String, Lookup>) suggesterConfigurationHandler
				.getDictionaryAnalyzer().getSuggesterList();

		Set<String> dictionaryNames = suggesters.keySet();

		boolean allSet = true;

		StringBuilder dictionariesNames = new StringBuilder();

		for (String dictionaryName : dictionaryProperties) {

			dictionariesNames.append(dictionaryProperties + "  ,   ");

			if ((!dictionaryNames.contains(dictionaryName))
					|| (suggesters.get(dictionaryName) == null)) {
				allSet = false;
				break;
			}

		}

		if (!allSet) {

			log.error("========= Available dictionary in eiddo===========");
			for (String name : dictionaryProperties) {
				log.error("\t\t" + name + "\t\t");
			}

			log.error("========= Available dictionary in applications===========");
			Iterator<String> enums = dictionaryNames.iterator();
			while (enums.hasNext()) {
				log.error("\t\t" + enums.next() + "\t\t");
			}
			log.error("===========================================================");

			log.error("Total number of Dictionary Names mismatch with Eiddo Dictionary Name , returning error code :500");

			return 500;
		} else {
			log.info("Dictionary Names : " + dictionariesNames.toString());
			log.info("No problem found in loaded Dictionaries");
		}

		/**************************************************************/

		return 200;

	}

	private int checkESConnection() {

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

}
