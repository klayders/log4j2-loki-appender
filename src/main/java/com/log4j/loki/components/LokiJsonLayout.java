package com.log4j.loki.components;


import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.jackson.Log4jJsonObjectMapper;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.apache.logging.log4j.core.util.StringBuilderWriter;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.system.ApplicationPid;

/**
 * Borrowed heavily from org.apache.logging.log4j.core.layout.JsonLayout for example see this https://stackoverflow.com/questions/39590365/print-stacktrace-with-log4j2-in-json-with-jsonlayout-in-a-single-line
 */
@Plugin(name = "LokiJsonLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class LokiJsonLayout extends AbstractStringLayout {

  private static final String CONTENT_TYPE = "application/json; charset=" + StandardCharsets.UTF_8.displayName();
  private static final String PID_KEY = "PID";
  private static final String HOSTNAME_KEY = "hostname";

  private final ObjectMapper objectMapper;
  protected static Map<String, String> additionalFields;
  public static int stackTraceLength;


  /**
   * @param encodeThreadContextAsList
   * @param includeStacktrace         - добавлять к ошибке стек трейс
   * @param stacktraceAsString        - сериализоваться стек трейс ввиде обычной строки (иначе ввиде json)
   * @param objectMessageAsJsonObject - пишет сообщения в json формате
   * @param keyValuePairs             - Дополнительные атрибуты, ключ - значение
   * @return возращает этот лайаут для логгирования ввиде json
   */

  @PluginFactory
  public static LokiJsonLayout createLayout(
      @PluginAttribute(value = "includeLocationInfo", defaultBoolean = true) final boolean includeLocationInfo,
      @PluginAttribute(value = "includeStacktrace", defaultBoolean = true) final boolean includeStacktrace,
      @PluginAttribute(value = "objectMessageAsJsonObject", defaultBoolean = true) final boolean objectMessageAsJsonObject,
      @PluginAttribute(value = "includePid", defaultBoolean = true) final boolean includePid,
      @PluginAttribute(value = "includePid", defaultBoolean = true) final boolean includeHostname,
      @PluginAttribute(value = "stacktraceAsString") final boolean stacktraceAsString,
      @PluginAttribute(value = "stackTraceLength", defaultInt = 50) final int stackTraceLength,
      @PluginAttribute(value = "encodeThreadContextAsList") final boolean encodeThreadContextAsList,
      @PluginElement("KeyValuePair") final KeyValuePair[] keyValuePairs
  ) {

    LokiJsonLayout.stackTraceLength = stackTraceLength;
    configureAdditionalMap(includePid, includeHostname, keyValuePairs);

    var log4jJsonObjectMapper = new Log4jJsonObjectMapper(
        encodeThreadContextAsList,
        includeStacktrace,
        stacktraceAsString,
        objectMessageAsJsonObject
    )
        .setDefaultPrettyPrinter(new MinimalPrettyPrinter())
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    return new LokiJsonLayout(log4jJsonObjectMapper);
  }

  private static void configureAdditionalMap(boolean includePid, boolean includeHostname, KeyValuePair[] keyValuePairs) {
    var additionalFields = new HashMap<String, String>();
    for (var keyValue : keyValuePairs) {
      additionalFields.put(keyValue.getKey(), keyValue.getValue());
    }

    if (includePid) {
      var pid = new ApplicationPid().toString();
      setSystemProperty(PID_KEY, pid);
      additionalFields.put(PID_KEY, pid);
    }

    if (includeHostname) {
      String hostname = null;
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
      additionalFields.put(HOSTNAME_KEY, hostname);
      System.out.println(HOSTNAME_KEY + ":" + hostname);
    }

    LokiJsonLayout.additionalFields = additionalFields;
  }

  LokiJsonLayout(ObjectMapper objectMapper) {
    super(StandardCharsets.UTF_8, null, null);
    this.objectMapper = objectMapper;
  }

  /**
   * Formats a {@link org.apache.logging.log4j.core.LogEvent}.
   *
   * @param event The LogEvent.
   * @return The JSON representation of the LogEvent.
   */
  @Override
  public String toSerializable(final LogEvent event) {
    final StringBuilderWriter writer = new StringBuilderWriter();
    try {
      //            objectMapper.writeValue(writer, wrap(event));
      objectMapper.writeValue(writer, LokiLogEvent.copyLogEvent(event));
      writer.write('\n');
      return writer.toString();
    } catch (final IOException e) {
      LOGGER.error(e);
      return Strings.EMPTY;
    }
  }

  @Override
  public String getContentType() {
    return CONTENT_TYPE;
  }


  private static void setSystemProperty(String name, String value) {
    if (System.getProperty(name) == null && value != null) {
      System.setProperty(name, value);
    }
  }
}
