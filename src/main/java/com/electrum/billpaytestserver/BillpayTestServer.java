/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electrum.billpaytestserver;

import java.io.IOException;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.electrum.billpaytestserver.engine.MockBillPayBackend;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 *
 * @author Mordechai
 */
public class BillpayTestServer extends ResourceConfig {

   private static final Logger log = LoggerFactory.getLogger(BillpayTestServer.class);

   public BillpayTestServer() throws IOException {
      JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
      JodaMapper jodaMapper = new JodaMapper();
      jodaMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      provider.setMapper(jodaMapper);

      register(provider);
      
      log.info("Loading packages");
      packages(BillpayTestServer.class.getPackage().getName());

      log.info("Initialising accounts");
      MockBillPayBackend.init();

   }

}
