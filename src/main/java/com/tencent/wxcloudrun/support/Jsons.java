package com.tencent.wxcloudrun.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class Jsons {

  private final ObjectMapper objectMapper;

  public Jsons(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Json serialization failed", exception);
    }
  }

  public Map<String, Object> toMap(Object value) {
    return objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {
    });
  }

  public Map<String, Object> readMap(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(value, new TypeReference<Map<String, Object>>() {
      });
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Json parse failed", exception);
    }
  }

  public Object readJson(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    try {
      return objectMapper.readValue(value, Object.class);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Json parse failed", exception);
    }
  }

  public List<String> readStringList(String value) {
    if (value == null || value.trim().isEmpty()) {
      return Collections.emptyList();
    }
    try {
      return objectMapper.readValue(value, new TypeReference<List<String>>() {
      });
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Json parse failed", exception);
    }
  }
}
