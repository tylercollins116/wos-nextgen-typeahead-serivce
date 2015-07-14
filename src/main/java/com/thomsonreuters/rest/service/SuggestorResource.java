package com.thomsonreuters.rest.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import com.thomsonreuters.models.Suggester;

@Singleton
@Api(value = "/suggest", description = "Suggest WS entry point")
@Path("/suggest")
public class SuggestorResource {

  private static final Logger logger = LoggerFactory.getLogger(SuggestorResource.class);

  @Configuration("1p.service.name")
  private Supplier<String> appName = Suppliers.ofInstance("One Platform");

  @Inject
  public SuggestorResource() {

  }

  
  @ApiOperation(value = "Suggest check", notes = "Returns list of suggestion for query prefix")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "RESPONSE_OK") })
  @Path("{query}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response helloTo(@PathParam("query") String query) {
    try {
    	ObjectMapper mapper = new ObjectMapper(  );
    	return Response.ok(mapper.writeValueAsString(Suggester.lookup(query, 10))).build();
    } catch (IOException e) {
      logger.error("Error creating json response.", e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}
