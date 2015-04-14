package com.xetus.freeipa.pwdportal

import groovy.transform.CompileStatic

@CompileStatic
class Response {

  String error
  String message
  
  Response(String error = null, String message = null) {
    this.error = error
    this.message = message
  }

}
