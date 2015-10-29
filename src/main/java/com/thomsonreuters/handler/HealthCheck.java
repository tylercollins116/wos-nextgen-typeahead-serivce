package com.thomsonreuters.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import netflix.karyon.health.HealthCheckHandler;

import org.apache.lucene.search.suggest.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
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

		for (String dictionaryName : dictionaryProperties) {
			
			log.error("Dictionary Names : "+dictionaryName);

			if ((!dictionaryNames.contains(dictionaryName))
					|| (suggesters.get(dictionaryName) == null)) {
				allSet = false;
				break;
			}

		}

		if (!allSet) {
			
			log.error("========= Available dictionary in eiddo===========");
			for (String name : dictionaryProperties){
				log.error("\t\t"+name+"\t\t");				
			}	
			
			log.error("========= Available dictionary in applications===========");
			Iterator<String> enums=dictionaryNames.iterator();
			while(enums.hasNext()){
				log.error("\t\t"+enums.next()+"\t\t");
			}
			log.error("===========================================================");


			log.error("Total number of Dictionary Names mismatch with Eiddo Dictionary Name , returning error code :500");

			return 500;
		}

		/**************************************************************/

		return 200;
	}
}
