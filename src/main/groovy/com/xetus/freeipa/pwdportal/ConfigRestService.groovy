package com.xetus.freeipa.pwdportal

import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Path("/config")
@Slf4j
@CompileStatic
class ConfigRestService {
  
  @Inject
  SiteConfig config
  
  @GET
  @Path("/recaptcha")
  @Produces("application/json")
  Map getConfig() {
    return [
      "enabled": config.isRecaptchaEnabled(),
      "recaptchaPublicKey": config.recaptchaPublicKey
    ]
  }

}
