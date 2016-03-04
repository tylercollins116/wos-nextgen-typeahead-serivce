package com.thomsonreuters.rest.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.governator.annotations.Configuration;
import com.thomsonreuters.models.SuggesterHandler;
import com.thomsonreuters.models.extact.SuggesterHandlerExt;

@Singleton
@Api(value = "/suggest", description = "Suggest WS entry point")
@Path("/suggest")
public class SuggestorResource {

	private static final Logger logger = LoggerFactory
			.getLogger(SuggestorResource.class);

	@Configuration("1p.service.name")
	private Supplier<String> appName = Suppliers.ofInstance("One Platform");

	private final SuggesterHandler suggesterHandler;
	private final SuggesterHandlerExt suggesterHandlerExt;

	@Inject
	public SuggestorResource(SuggesterHandler suggesterHandler,
			SuggesterHandlerExt suggesterHandlerExt) {
		this.suggesterHandler = suggesterHandler;
		this.suggesterHandlerExt = suggesterHandlerExt;
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@Path("{path}/{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response helloTo(@PathParam("path") String path,
			@PathParam("query") String query) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(
					mapper.writeValueAsString(suggesterHandler.lookup(path,
							query, 10))).build();
		} catch (IOException e) {
			logger.error("Error creating json response.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@Path("{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchQuery(@PathParam("query") String query) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(
					mapper.writeValueAsString(suggesterHandler
							.lookup(query, 10))).build();
		} catch (IOException e) {
			logger.error("Error creating json response.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchWithQueryParam(@QueryParam("query") String query,
			@QueryParam("source") List<String> source,
			@QueryParam("info") List<String> info,
			@DefaultValue("10") @QueryParam("size") int size,
			@QueryParam("uid") String uid) {

		/**
		 * example of executing endpoints
		 * 
		 * http://localhost:7001/suggest?query=medi
		 * &sources=wos&sources=countries&info=health&info=sports
		 * 
		 * **/

		try {

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(
					mapper.writeValueAsString(suggesterHandler.lookup(query,
							source, info, size, uid))).build();
		} catch (IOException e) {
			logger.error("Error creating json response.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@GET
	@Path("/preterms")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchWithQueryParam(@QueryParam("query") String query,
			@DefaultValue("10") @QueryParam("size") int size,
			@QueryParam("uid") String uid,
			@DefaultValue("false") @QueryParam("showall") boolean showall) {

		/**
		 * example of executing endpoints
		 * 
		 * http://localhost:7001/suggest?query=medi
		 * &sources=wos&sources=countries&info=health&info=sports
		 * 
		 * **/

		try {

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(
					mapper.writeValueAsString(suggesterHandler.lookup(query,
							size, uid, showall))).build();
		} catch (IOException e) {
			logger.error("Error creating json response.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@Path("/ext/act")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchQuery(@QueryParam("query") String query,
			@QueryParam("source") List<String> source,
			@QueryParam("info") List<String> info,
			@DefaultValue("10") @QueryParam("size") int size) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return Response.ok(
					mapper.writeValueAsString(suggesterHandlerExt.lookup(query,
							source, info, size))).build();
		} catch (IOException e) {
			logger.error("Error creating json response.", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.build();
		}
	}

}
