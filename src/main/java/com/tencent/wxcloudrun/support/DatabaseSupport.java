package com.tencent.wxcloudrun.support;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DatabaseSupport {

  private static final String DEFAULT_NAMESPACE = "CityConnectMapper.";
  private final SqlSessionTemplate sqlSessionTemplate;

  public DatabaseSupport(SqlSessionTemplate sqlSessionTemplate) {
    this.sqlSessionTemplate = sqlSessionTemplate;
  }

  public Optional<Map<String, Object>> findOne(String sql, Map<String, ?> params) {
    return Optional.ofNullable(sqlSessionTemplate.selectOne(statementId(sql), safeParams(params)));
  }

  public List<Map<String, Object>> findAll(String sql, Map<String, ?> params) {
    return sqlSessionTemplate.selectList(statementId(sql), safeParams(params));
  }

  public int update(String sql, Map<String, ?> params) {
    return sqlSessionTemplate.update(statementId(sql), safeParams(params));
  }

  public Number queryNumber(String sql, Map<String, ?> params) {
    return sqlSessionTemplate.selectOne(statementId(sql), safeParams(params));
  }

  public String queryString(String sql, Map<String, ?> params) {
    return sqlSessionTemplate.selectOne(statementId(sql), safeParams(params));
  }

  private String statementId(String value) {
    if (value == null) {
      throw new IllegalArgumentException("statementId must not be null");
    }
    return value.contains(".") ? value : DEFAULT_NAMESPACE + value;
  }

  private Map<String, ?> safeParams(Map<String, ?> params) {
    return params == null ? Collections.emptyMap() : params;
  }
}
