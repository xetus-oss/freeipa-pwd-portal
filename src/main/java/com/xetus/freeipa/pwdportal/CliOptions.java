package com.xetus.freeipa.pwdportal;

import java.io.File;

import org.kohsuke.args4j.Option;

class CliOptions {
  @Option(
    name = "-p",
    aliases = { "--port" },
    metaVar = "<port-number>",
    usage = "The port number to run the api server on"
  )
  Integer port = 8999;

  @Option(
    name = "-kf",
    aliases = { "--keystore-file" },
    metaVar = "<keystore-file>",
    usage = "The path to the certificate's containing keystore",
    required = true
  )
  File keystoreFile = null;

  @Option(
    name = "-kp",
    aliases = { "--keystore-pass" },
    metaVar = "<keystore-pass>",
    usage = "The password for certificate's containing keystore",
    required = true
  )
  String keystorePass = null;

  @Option(
    name = "-ka",
    aliases = { "--keystore-alias" },
    metaVar = "<keystore-alias>",
    usage = "The alias in the certificate's containing keystore to use",
    required = true
  )
  String keystoreAlias = null;
  
  public CliOptions() {}
  
  void setPort(Integer port) {
    this.port = port;
  }
  
  void setKeystoreFile(File keystorePath) {
    this.keystoreFile = keystorePath;
  }
  
  void setKeystorePass(String keystorePass) {
    this.keystorePass = keystorePass;
  }
  
  void setKeystoreAlias(String keystoreAlias) {
    this.keystoreAlias = keystoreAlias;
  }
  
  Integer getPort() {
    return this.port;
  }
  
  File getKeystoreFile() {
    return this.keystoreFile;
  }
  
  String getKeystorePass() {
    return this.keystorePass;
  }
  
  String getKeystoreAlias() {
    return this.keystoreAlias;
  }
    
}
