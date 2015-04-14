package com.xetus.freeipa.pwdportal

import org.codehaus.groovy.control.CompilerConfiguration

import com.xetus.iris.FreeIPAConfig

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class SiteConfigLoader {

  public static final String CONFIG_DIR_VAR = "com.xetus.freeipa-pwd-portal.config"
  
  /**
   * The mechanism for loading the {@link SiteConfig} from the host system.
   * Note that evaluation of the final {@link SiteConfig} is cumulative; 
   * values from previously evaluated configuration files during the execution
   * of this method will only be overwritten if the field has an assignment
   * in one of the next evaluated configuration files.
   * 
   * The locations that will be searched for configuration files are: <ol>
   * 
   *  <li>/defaultconfig.groovy of the war's resources directory
   *  <li>/etc/freeipa-pwd-portal/siteconfig.groovy if the {@link 
   *  #CONFIG_DIR_VAR} environment variable is not set, or
   *  <li>siteconfig.groovy within the directory indicated by the {@link
   *  #CONFIG_DIR_VAR} environment variable if it is set
   *   
   * @return the {@link SiteConfig} resulting from merging the configuration
   * files found in the above locations.
   */
  static SiteConfig buildConfig() {
    def cc = new CompilerConfiguration()
    def cl = SiteConfigLoader.class.classLoader

    cc.setScriptBaseClass(DelegatingScript.class.name);
    def shell = new GroovyShell(cl, new Binding(), cc)
    
    DelegatingScript script = (DelegatingScript) shell.parse(
       SiteConfigLoader.class.getResource("/defaultconfig.groovy").toURI())
    
    def config = new SiteConfig()
    script.setDelegate(config)
    script.run()
    
    File externalConfigDir = new File("/etc/freeipa-pwd-portal/")
    if (System.getProperty(CONFIG_DIR_VAR)){
      externalConfigDir = new File(
          System.getProperty(CONFIG_DIR_VAR))
    } else if (System.getenv()["FREEIPA_PWD_PORTAL_CONFIG"]){
      externalConfigDir = new File(System.getenv()["FREEIPA_PWD_PORTAL_CONFIG"])
    }
    config.externalConfigDir = externalConfigDir
    File overrideFile = new File(externalConfigDir, "siteconfig.groovy")

    if (externalConfigDir.exists()){
      script = (DelegatingScript) shell.parse(overrideFile.text)
      script.setDelegate(config)
      script.run()
    }
    
    log.debug("Loaded config: \n\n$config")
    
    return config
  }

}
