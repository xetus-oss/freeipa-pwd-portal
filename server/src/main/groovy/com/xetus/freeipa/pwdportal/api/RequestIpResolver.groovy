package com.xetus.freeipa.pwdportal.api;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import com.xetus.freeipa.pwdportal.PwdPortalConfig;

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@Component
@CompileStatic
public class RequestIpResolver {
  
  PwdPortalConfig config
  
  @Autowired
  public RequestIpResolver(PwdPortalConfig config) {
    this.config = config;
  }
  
  public String resolve(HttpServletRequest request) {
    String remoteIp = request.getRemoteAddr()
    String xForwardedForValue = request.getHeader(config.xForwardedForHeader)
    if (xForwardedForValue) {
      log.debug("using xForwardedForValue: ", 
                config.getxForwardedForHeader(), xForwardedForValue)
      remoteIp = xForwardedForValue.split(',')[0]?.trim()
    }
    
    return remoteIp
  }
}
