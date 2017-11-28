package com.xetus.freeipa.pwdportal.ipa;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.xetus.oss.iris.FreeIPAConfig;
import com.xetus.oss.iris.jackson.databind.ObjectMapperBuilder;

import groovy.transform.CompileStatic;
import groovy.transform.ToString;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A small config adapter class intended to enabling loading 
 * the Iris {@link FreeIPAConfig} values through Spring's configuration
 * loading mechanism
 */
@ToString(
    includeFields = true, 
    includeNames = true,
    includeSuperProperties = true
)
@Component
@CompileStatic
@ConfigurationProperties("iris")
public class IrisConfig extends FreeIPAConfig {
  @Override
  public ObjectMapper getRPCObjectMapper() {
    return ObjectMapperBuilder.getObjectMapper();
  }
}
