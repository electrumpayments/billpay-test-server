/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electrum.billpaytestserver.ws;

import java.util.Calendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/health")
public class HealthResource {
   private static final Logger logger = LoggerFactory.getLogger(HealthResource.class);

   
   private Calendar calendar;

   public HealthResource() {
      calendar = Calendar.getInstance();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/ping")
   public Response pong() {
      logger.info("Ping in");
      JSONObject responseJson = new JSONObject();
      try {
          responseJson.put("message", "pong");
          responseJson.put("date", calendar.getTime());
      } catch (JSONException e) {
         e.printStackTrace();
      }

      return Response.status(200).entity(responseJson).build();
   }


}
