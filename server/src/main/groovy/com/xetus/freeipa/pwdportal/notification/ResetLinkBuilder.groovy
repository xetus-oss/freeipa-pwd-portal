package com.xetus.freeipa.pwdportal.notification;

import com.xetus.freeipa.pwdportal.ipa.reset.ResetRequest

import groovy.transform.CompileStatic;

@CompileStatic
public class ResetLinkBuilder {
  String webUri;
  
  public ResetLinkBuilder(String webUri) {
    this.webUri = webUri;
  }
  
  public String build(ResetRequest request) {
    return this.webUri + "#/reset/" + request.getResetId();
  }
}
