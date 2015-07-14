package com.thomsonreuters.rest.service;

import java.io.IOException;

import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.KaryonServer;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrap;

import org.codehaus.jettison.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import com.netflix.governator.guice.BootstrapModule;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.thomsonreuters.eiddo.EiddoPropertiesLoader;
import com.thomsonreuters.handler.HealthCheck;
import com.thomsonreuters.injection.BootstrapInjectionModule;
import com.thomsonreuters.injection.SwaggerHystrixModule;
import com.thomsonreuters.injection.module.MainModule;
import com.thomsonreuters.rest.service.SuggestorTest.TestInjectionModule.TestModule;

/*TODO write more test */

public class SuggestorTest extends JerseyTest {
	private static final int PORT = 7001;
	private static final String baseUrl = "http://localhost:" + PORT + "/";
	private static KaryonServer server;

	@ArchaiusBootstrap(loader = EiddoPropertiesLoader.class)
	@KaryonBootstrap(name = "junit", healthcheck = HealthCheck.class)
	@Singleton
	@Modules(include = { ShutdownModule.class, TestModule.class,
			SwaggerHystrixModule.class,
			BootstrapInjectionModule.KaryonRxRouterModuleImpl.class, })
	public interface TestInjectionModule {
		public static class TestModule extends MainModule {

			@Override
			protected void configure() {
				// bind(HealthCheck.class).toInstance(mockHealthCheck);
			}
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			System.setProperty("eiddo.client.repoChain", "junit");
			System.setProperty("eiddo.repo.junit.urlTemplate",
					"https://eiddo.1p.thomsonreuters.com/r/junit");
			// System.setProperty("eiddo.repo.junit.urlTemplate",
			// "http://eiddo-1135711100.us-west-2.elb.amazonaws.com/r/junit");
			System.setProperty("eiddo.repo.junit.username", "junit");
			System.setProperty("eiddo.repo.junit.password", "junit");
			server = Karyon.forApplication(TestInjectionModule.class,
					(BootstrapModule[]) null);
			server.start();
			System.out.println("Karyon server started");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		server.shutdown();
	}

	public SuggestorTest() {
		super("com.thomsonreuters.rest.service");
	}

	@Override
	protected AppDescriptor configure() {
		return new WebAppDescriptor.Builder().build();
	}

	@Test
	public void testHealthCheck() throws JSONException,
			JsonProcessingException, IOException {
		/*
		 * TODO write more test
		 */
		// WebResource webResource = client().resource(baseUrl);
		// ClientResponse response = webResource.path("/healthcheck")
		// .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		// String json = response.getEntity(String.class);
		// System.out.println(json);
		// ObjectMapper m = new ObjectMapper();
		// JsonNode root = m.readTree(json);
		// Assert.assertNotNull(root);
		// Assert.assertTrue(json.contains("One Platform JUnit Overriden by Eiddo"));
	}

}
