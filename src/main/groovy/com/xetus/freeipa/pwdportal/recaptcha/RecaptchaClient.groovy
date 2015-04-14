package com.xetus.freeipa.pwdportal.recaptcha

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.io.DataOutputStream
import java.net.HttpURLConnection

import javax.net.ssl.HttpsURLConnection

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Quick and dirty Recaptcha response verification client to
 * hit Google's recaptcha API endpoint and determine the validity
 * of the user's response.
 * 
 * see https://developers.google.com/recaptcha/docs/verify for details
 * 
 * TODO: split this into a separate tiny library
 */
@Slf4j
@CompileStatic
class RecaptchaClient {

  static final String ENDPOINT = 
    "https://www.google.com/recaptcha/api/siteverify"

  String privateKey
  
  RecaptchaClient(String key) {
    this.privateKey = key
  }
  
  String toParams(String response, String remoteIp) {
    
    if (!privateKey) {
      throw new IllegalStateException("Private key must be configured")
    }
    
    String params = "secret=$privateKey&response=$response"
    if (remoteIp) {
      params += "&remoteip=$remoteIp"
    }
    
    params
  }
  
  RecaptchaResponse verify(String response, String remoteIp = null) {
    
    HttpURLConnection con
    try {
      
      String params = toParams(response, remoteIp)
      con = (HttpsURLConnection) new URL(ENDPOINT).openConnection()
      
      con.setDoOutput(true)
      con.setDoInput (true)
      con.setInstanceFollowRedirects(false)
      con.setRequestMethod("POST")
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      con.setRequestProperty("charset", "UTF-8")
      con.setRequestProperty("Content-Length", 
                             Integer.toString(params.bytes.length))
      con.setUseCaches(false)
      
      DataOutputStream wr = new DataOutputStream(con.getOutputStream())
      wr.writeBytes(params)
      wr.flush()
      wr.close()
      
      con.connect()
      
      BufferedInputStream is = new BufferedInputStream(con.getInputStream())
      String json = is.readLines()?.join("")
      is.close()
      
      log.debug "Received JSON from Google: $json"
      
      return new ObjectMapper().readValue(json, RecaptchaResponse.class)
      
    } finally {
      if (con != null) {
        con?.disconnect()
      }
    }

  }
  
}
