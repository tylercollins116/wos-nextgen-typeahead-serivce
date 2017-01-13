package com.thomsonreuters.injection;

import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
import netflix.karyon.eureka.KaryonEurekaModule;
import netflix.karyon.servo.KaryonServoModule;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import com.thomsonreuters.eiddo.client.EiddoPropertiesLoader;
import com.thomsonreuters.events.karyon.EventsModule;
import com.thomsonreuters.handler.HealthCheck;
import com.thomsonreuters.injection.module.MainModule;
import com.thomsonreuters.karyon.JerseyBasicRoutingModule;
import com.thomsonreuters.karyon.ShutdownModule;
import com.thomsonreuters.logback.LogbackEiddoModule;

@ArchaiusBootstrap(loader = EiddoPropertiesLoader.class)
@KaryonBootstrap(name = "1p-typeahead-service", healthcheck = HealthCheck.class)
@Singleton

@Modules(include = {
		ShutdownModule.class,
		KaryonServoModule.class,
		KaryonWebAdminModule.class,
		EventsModule.class,
		MainModule.class,
		LogbackEiddoModule.class,
		KaryonEurekaModule.class,
		JerseyBasicRoutingModule.class
})
public interface BootstrapInjectionModule {
	class KaryonRxRouterModuleImpl extends JerseyBasicRoutingModule {
		@Override
		protected void configureServer() {
			super.configureServer();
		}
	}

}