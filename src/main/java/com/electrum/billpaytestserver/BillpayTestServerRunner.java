/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.electrum.billpaytestserver;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.glassfish.jersey.servlet.ServletContainer;

public class BillpayTestServerRunner {

   public static void main(String[] args) throws Exception {
      // === jetty.xml ===
      // Setup Threadpool
      QueuedThreadPool threadPool = new QueuedThreadPool();
      threadPool.setMaxThreads(500);

      // Server
      Server server = new Server(threadPool);

      // Scheduler
      server.addBean(new ScheduledExecutorScheduler());

      // HTTP Configuration
      HttpConfiguration http_config = new HttpConfiguration();
      http_config.setSecureScheme("https");
      // http_config.setSecurePort(8443);
      http_config.setSecurePort(Integer.parseInt(args[0]));
      http_config.setOutputBufferSize(32768);
      http_config.setRequestHeaderSize(8192);
      http_config.setResponseHeaderSize(8192);
      http_config.setSendServerVersion(true);
      http_config.setSendDateHeader(false);

      // Handler Structure
      HandlerCollection handlers = new HandlerCollection();
      ContextHandlerCollection contexts = new ContextHandlerCollection();
      handlers.setHandlers(new Handler[] { contexts, new DefaultHandler() });
      server.setHandler(handlers);

      // Extra options
      server.setDumpAfterStart(false);
      server.setDumpBeforeStop(false);
      server.setStopAtShutdown(true);

      // === jetty-http.xml ===
      ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
      // http.setPort(8080);
      http.setPort(Integer.parseInt(args[0]));
      http.setIdleTimeout(30000);
      server.addConnector(http);

      BillpayTestServer motherOfMonsters = new BillpayTestServer();
      ServletContainer servletContainer = new ServletContainer(motherOfMonsters);
      ServletHolder servletHolder = new ServletHolder(servletContainer);

      ServletContextHandler context = new ServletContextHandler();
      context.setContextPath("/");
      context.addServlet(servletHolder, "/*");

      FilterHolder corsFilterHolder = new FilterHolder(CrossOriginFilter.class);
      corsFilterHolder.setName("CORS filter");
      context.addFilter(corsFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
      corsFilterHolder.getRegistration().setInitParameter("allowedOrigins", "*");
      corsFilterHolder.getRegistration().setInitParameter(
            "allowedHeaders",
            "Access-Control-Allow-Headers,Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers");
      corsFilterHolder.getRegistration().setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");

      server.setHandler(context);

      // Start the server
      server.start();
      server.join();
   }

}
