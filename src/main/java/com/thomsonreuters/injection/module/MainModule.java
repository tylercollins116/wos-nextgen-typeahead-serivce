package com.thomsonreuters.injection.module;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.thomsonreuters.models.Suggester;
import com.thomsonreuters.models.SuggesterConfiguration;
import com.thomsonreuters.models.SuggesterConfigurationHandler;
import com.thomsonreuters.models.SuggesterHandler;
public class MainModule extends AbstractModule {
    @Override
    protected void configure() {
       //Guice bindings goes here
    	

		bind(SuggesterConfigurationHandler.class).to(
				SuggesterConfiguration.class).in(Singleton.class);
		bind(SuggesterHandler.class).to(Suggester.class).in(Singleton.class);
    }
}
