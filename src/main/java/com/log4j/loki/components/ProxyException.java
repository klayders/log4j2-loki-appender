package com.log4j.loki.components;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProxyException {

  private String message;
  private String localizedMessage;
  private String detailMessage;
  private StackTraceElement[] stackTrace;

  public static ProxyException of(Throwable throwable) {
    var stackTrace = throwable.getStackTrace();

    return ProxyException.builder()
        .message(throwable.getMessage())
        .localizedMessage(throwable.getLocalizedMessage())
        .stackTrace(Arrays.copyOfRange(stackTrace, 0, LokiJsonLayout.stackTraceLength))
        .build();
  }
}
