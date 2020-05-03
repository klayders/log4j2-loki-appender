package com.log4j.loki.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyStackTrace {

  @JsonProperty("class")
  private String clazz;
  private String method;
  private String file;
  private int line;
  private String moduleName;

  public static ProxyStackTrace of(StackTraceElement stackTraceElement) {
    return ProxyStackTrace.builder()
        .clazz(stackTraceElement.getClassName())
        .method(stackTraceElement.getMethodName())
        .file(stackTraceElement.getFileName())
        .line(stackTraceElement.getLineNumber())
        .build();
  }

}
