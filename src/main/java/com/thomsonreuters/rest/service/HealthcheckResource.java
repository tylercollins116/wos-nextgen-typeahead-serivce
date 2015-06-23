package com.thomsonreuters.rest.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import netflix.karyon.health.HealthCheckHandler;

import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/*
 * TODO Fix Health Check
 */

@Api(value = "/healthcheck", description = "Health check entry point")
@Path("/healthcheck")
public class HealthcheckResource {

  private final HealthCheckHandler healthCheckHandler;

  @Inject
  public HealthcheckResource(HealthCheckHandler healthCheckHandler) {
    this.healthCheckHandler = healthCheckHandler;
  }

  @ApiOperation(value = "Health check", notes = "Returns result of the health check")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Healthy") })
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response healthcheck() {
    return Response.status(healthCheckHandler.getStatus()).build();
  }
}
