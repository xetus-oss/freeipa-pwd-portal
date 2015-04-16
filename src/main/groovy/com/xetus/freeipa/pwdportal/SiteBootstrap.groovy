package com.xetus.freeipa.pwdportal

import javax.annotation.PostConstruct
import javax.inject.Inject

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.util.ContextInitializer
import groovy.util.logging.Slf4j

@Slf4j
@Service
class SiteBoostrap {
  
  @Inject SiteConfig siteConfig
  
  @PostConstruct
  void boot(){
    
    /*
     * Configure external logging
     */
    if  (siteConfig.externalConfigDir){
      LoggerContext lc = LoggerFactory.ILoggerFactory
      ContextInitializer ci = new ContextInitializer(lc)
      
      def logbackOverrides =
      new File(siteConfig.externalConfigDir, "logback.groovy")
      
      if (logbackOverrides.exists()){
        println "Intializing logback override from: " + logbackOverrides
        ci.configureByResource(logbackOverrides.toURI().toURL())
      }
    }
  }
  
}