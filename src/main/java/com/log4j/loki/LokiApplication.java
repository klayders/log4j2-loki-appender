package com.log4j.loki;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j
@SpringBootApplication
public class LokiApplication {

  public static void main(String[] args) {
    SpringApplication.run(LokiApplication.class, args);
  }

}
