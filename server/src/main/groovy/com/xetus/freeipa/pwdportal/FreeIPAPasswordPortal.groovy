package com.xetus.freeipa.pwdportal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper
import com.xetus.freeipa.pwdportal.ipa.IrisConfig;
import com.xetus.oss.iris.FreeIPAAuthenticationManager;

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
@SpringBootApplication
@EnableConfigurationProperties
public class FreeIPAPasswordPortal {
  public static void main(String[] args){
    SpringApplication.run(FreeIPAPasswordPortal.class, args);
  }

  @Bean
  @Autowired
  FreeIPAAuthenticationManager freeIPAAuthenticationManager(IrisConfig config) {
    if (log.isDebugEnabled()) {
      log.debug("initizliing FreeIPAAuthenticationManager with config: {}", config);
    }
    return new FreeIPAAuthenticationManager(config);
  }
}
