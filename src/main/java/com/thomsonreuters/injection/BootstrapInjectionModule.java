package com.thomsonreuters.injection;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import com.thomsonreuters.eiddo.client.EiddoPropertiesLoader;
import com.thomsonreuters.handler.HealthCheck;
import com.thomsonreuters.injection.module.MainModule;
import com.thomsonreuters.karyon.JerseyBasicRoutingModule;
import com.thomsonreuters.karyon.ShutdownModule;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.archaius.ArchaiusBootstrap;
import netflix.karyon.eureka.KaryonEurekaModule;
import netflix.karyon.servo.KaryonServoModule;

@ArchaiusBootstrap(loader = EiddoPropertiesLoader.class)
@KaryonBootstrap(name = "1p-typeahead-service", healthcheck = HealthCheck.class)
@Singleton
@Modules(include = { ShutdownModule.class, KaryonServoModule.class,
		KaryonWebAdminModule.class, KaryonEurekaModule.class,  MainModule.class,
		BootstrapInjectionModule.KaryonRxRouterModuleImpl.class, })
public interface BootstrapInjectionModule {
	class KaryonRxRouterModuleImpl extends JerseyBasicRoutingModule {

		public KaryonRxRouterModuleImpl() {
		}

		@Override
		protected void configureServer() {
			// replace default behavior (port,pool size) if needed
			super.configureServer();
		}
	}

}
