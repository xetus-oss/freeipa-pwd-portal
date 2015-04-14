beans {

  xmlns([ctx:'http://www.springframework.org/schema/context'])
  ctx.'component-scan'('base-package': "com.xetus.freeipa.pwdportal")
  ctx.'annotation-config'(true)
  
  /*
   * Go build a SiteConfig object, using the pre-defined override process
   */
  siteConfig(org.springframework.beans.factory.config.MethodInvokingFactoryBean){
    targetClass = com.xetus.freeipa.pwdportal.SiteConfigLoader
    targetMethod = "buildConfig"
  }
}