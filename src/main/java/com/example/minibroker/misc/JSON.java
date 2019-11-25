package com.example.minibroker.misc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON
 *
 * @author jeffsky
 * @since 2018-08-20
 */
public final class JSON {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OBJECT_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
  }

  private JSON() {
    throw new RuntimeException("unreachable");
  }

  public static <T> String stringify(T t) {
    if (t == null) {
      return "null";
    }
    try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
      final JsonGenerator factory =
          OBJECT_MAPPER.getFactory().createGenerator(bos, JsonEncoding.UTF8);
      factory.writeObject(t);
      return new String(bos.toByteArray(), Charsets.UTF_8);
    } catch (IOException e) {
      log.error("stringify to json failed:", e);
      throw Throwables.propagate(e);
    }
  }

  public static <T> T parse(String json, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(json, clazz);
    } catch (Throwable e) {
      log.error("parse json failed: json={}", json, e);
      throw Throwables.propagate(e);
    }
  }
}
