package io.electrum.billpaytestserver;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 *
 */
public class Utils {

   private static ObjectMapper objectMapper = new ObjectMapper();

   static {
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
      objectMapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
      objectMapper.registerModule(new JodaModule());
      objectMapper.setDateFormat(getDefaultDateFormat());
   }

   public static DateFormat getDefaultDateFormat() {
      // Use RFC3339 format for date and datetime.
      // See http://xml2rfc.ietf.org/public/rfc/html/rfc3339.html#anchor14
      DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
      dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
      return dateFormat;
   }

   private static ObjectWriter objectWriter = objectMapper.writer();

   public static String objectToPrettyPrintedJson(Object object) throws JsonProcessingException {
      return objectWriter.withDefaultPrettyPrinter().writeValueAsString(object);
   }

   public static String objectToJson(Object object) throws JsonProcessingException {
      return objectWriter.writeValueAsString(object);
   }

   public static ObjectMapper getObjectMapper() {
      return objectMapper;
   }

   public static String generateIssuerReferenceNumber() {
      Random randomGenerator = new Random();
      return randomGenerator.nextInt(10000000) + "";
   }
}
