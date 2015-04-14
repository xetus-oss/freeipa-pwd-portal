package com.xetus.freeipa.pwdportal;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.connector.Connector;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

class EmbeddedRunner {
  
  public static void main(String[] args) 
      throws URISyntaxException, ServletException, LifecycleException {
    
    CliOptions opts = new CliOptions();
    CmdLineParser parser = new CmdLineParser(opts);
    try {
      parser.setUsageWidth(150);
      parser.parseArgument(args);
    } catch (CmdLineException cle){
      System.err.println(cle.getMessage() + "\n");
      parser.printUsage(System.err);
      return;
    } catch(Exception e) {
      e.printStackTrace();
    }

    File warPath = new File(
      EmbeddedRunner.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath());

    File base = new File(System.getProperty("java.io.tmpdir"));
    base = new File(base, "freeipapwdportal." + opts.getPort());
    base.delete();
    base.mkdirs();
    System.out.println("Running out of " + base.getAbsolutePath());
    
    /*
     * For CXF, to so it uses SL4J logging
     */
    System.setProperty("org.apache.cxf.Logger",
      "org.apache.cxf.common.logging.Slf4jLogger");
    List<String> subdirs = new ArrayList<String>();
    subdirs.add("webapps");
    subdirs.add("work");
    subdirs.add("temp");
    
    for (String dir : subdirs) {
      new File(base, dir).mkdirs();
    }
   
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(opts.getPort());
    tomcat.setBaseDir(base.getAbsolutePath());

    Connector connector = tomcat.getConnector();
    connector.setPort(opts.getPort());
    connector.setMaxPostSize(1000);
    connector.setEnableLookups(false);
    connector.setAttribute("disableUploadTimeout", false);  
    connector.setAttribute("compression", true);
    connector.setAttribute("compressableMimeType",
        "text/html,text/xml,text/plain,text/javascript,text/css");
  
    connector.setScheme("https");
    connector.setSecure(true);
    connector.setAttribute("keystorePass", opts.getKeystorePass());
    connector.setAttribute("keystoreFile", opts.getKeystoreFile());
    connector.setAttribute("keyAlias", opts.getKeystoreAlias());      
    connector.setAttribute("clientAuth", false);
    // TLS disabled due to CVE-2014-3566
    // YOU MUST ENABLE SSLv2HELLO for older clients! This does not enable SSLv2
    connector.setAttribute("sslEnabledProtocols", "TLSv1,SSLv2Hello");
    connector.setAttribute("SSLEnabled", true);
    connector.setAttribute("ciphers", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256"
        + ",TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"
        + ",TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384"
        + ",TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"
        + ",TLS_ECDHE_RSA_WITH_RC4_128_SHA"
        + ",TLS_RSA_WITH_AES_128_CBC_SHA256"
        + ",TLS_RSA_WITH_AES_128_CBC_SHA"
        + ",TLS_RSA_WITH_AES_256_CBC_SHA256"
        + ",TLS_RSA_WITH_AES_256_CBC_SHA,SSL_RSA_WITH_RC4_128_SHA");

    tomcat.addWebapp("/", warPath.getAbsolutePath());
    tomcat.start();
    tomcat.getServer().await();
  }
}