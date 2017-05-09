package com.thomsonreuters.rest.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.governator.annotations.Configuration;
import com.thomsonreuters.models.SuggesterHandler;
import com.thomsonreuters.models.services.suggesterOperation.IPA.IPASuggesterHandler;

@Singleton
@Api(value = "/suggest", description = "Suggest WS entry point")
@Path("/suggest")
public class SuggestorResource {

	private static final Logger logger = LoggerFactory
			.getLogger(SuggestorResource.class);

	@Configuration("1p.service.name")
	private Supplier<String> appName = Suppliers.ofInstance("One Platform");

	private final SuggesterHandler suggesterHandler;
	private final IPASuggesterHandler ipaSuggesterHandler;

	@Inject
	public SuggestorResource(SuggesterHandler suggesterHandler,IPASuggesterHandler ipaSuggesterHandler) {
		this.suggesterHandler = suggesterHandler;
		this.ipaSuggesterHandler=ipaSuggesterHandler;
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@Path("{path}/{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response helloTo(@PathParam("path") String path,
			@PathParam("query") String query, 
			@DefaultValue("false") @QueryParam("highlight") boolean highlight) {

		return Response.ok(suggesterHandler.lookup(path, query, 10, highlight)).build();
	}

	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@Path("{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchQuery(@PathParam("query") String query) {

		return Response.ok(suggesterHandler.lookup(query, 10)).build();
	}

	@ApiOperation(value = "IPA Suggest", notes = "Returns list of suggestion for query")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchByElastic(@Context HttpHeaders headers,
			@QueryParam("query") String query,
			@QueryParam("source") String source,
			@DefaultValue("0") @QueryParam("offset") int offset,
			@DefaultValue("10") @QueryParam("size") int size,
			@DefaultValue("false") @QueryParam("highlight") boolean highlight,
			@QueryParam("uid") String uid) {

			if (uid == null || uid.trim().length() == 0) {
				uid = Utils.getUserid(headers);
				logger.info("User Id from Header " + uid);
			}
			return Response.ok(suggesterHandler.lookup(query, source, offset, size, uid, highlight)).build();
	}
	
	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchWithQueryParam(@Context HttpHeaders headers,
			@QueryParam("query") String query,
			@QueryParam("source") List<String> source,
			@QueryParam("info") List<String> info,
			@DefaultValue("10") @QueryParam("size") int size,
			@DefaultValue("false") @QueryParam("highlight") boolean highlight,
			@QueryParam("uid") String uid) {

		/**
		 * example of executing endpoints
		 * 
		 * http://localhost:7001/suggest?query=medi
		 * &sources=wos&sources=countries&info=health&info=sports
		 * 
		 * **/
		if (uid == null || uid.trim().length() <= 0) {
			uid = Utils.getUserid(headers);
			logger.info("User Id from Header" + uid);
		}

		return Response.ok(suggesterHandler.lookup(query, source, info, size, uid, highlight)).build();
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
		return Response.ok(suggesterHandler.lookup(query, size, uid, showall, false)).build();
	}
	
	@ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
	 
	@Path("/ipa/{path}/{query}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchQuery(@PathParam("path") String path,
			@PathParam("query") String query,
			@DefaultValue("10") @QueryParam("size") int size,
			@DefaultValue("true") @QueryParam("countchild") boolean countchild,
			@DefaultValue("false") @QueryParam("showall") boolean showall) {

		return Response.ok(ipaSuggesterHandler.lookup(path, query,size,countchild,showall)).build();
	}
	
}
