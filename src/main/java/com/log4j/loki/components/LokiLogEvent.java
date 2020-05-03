package com.log4j.loki.components;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LokiLogEvent {

  @JsonProperty("ts")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXZ")
  private Date ts;
  private Level level;
  private String pid;

  private String loggerName;
  private Message message;
  private ProxyException exception;

  private ThreadContext.ContextStack contextStack = ThreadContext.getImmutableStack();
  private String threadName;
  private StackTraceElement source;

  public static LokiLogEvent copyLogEvent(LogEvent logEvent) {

    ProxyException proxyException = null;
    if (logEvent.getThrown() != null) {
      proxyException = ProxyException.of(logEvent.getThrown());
    }

    return LokiLogEvent.builder()
        .ts(new Date(logEvent.getTimeMillis()))
        .level(logEvent.getLevel())
        .loggerName(logEvent.getLoggerName())
        .message(logEvent.getMessage())
        .contextStack(logEvent.getContextStack())
        .exception(proxyException)
        .threadName(logEvent.getThreadName())
        .source(logEvent.getSource())
        .build();
  }

  @JsonAnyGetter
  public Map<String, String> getAdditionalFields() {
    return LokiJsonLayout.additionalFields;
  }

}
