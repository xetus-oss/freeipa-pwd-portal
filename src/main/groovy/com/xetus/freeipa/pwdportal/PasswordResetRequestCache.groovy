package com.xetus.freeipa.pwdportal

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.util.concurrent.CopyOnWriteArrayList

/**
 * A simple cache of {@link PasswordResetRequest} objects that
 * enforces expiration and uniqueness of it's contained requests.
 */
@ToString
@CompileStatic
class PasswordResetRequestCache {

  static PasswordResetRequestCache INSTANCE = null
  static PasswordResetRequestCache getInstance() {
    if (!INSTANCE) {
      INSTANCE = new PasswordResetRequestCache()
    }
    INSTANCE
  }
  
  List<PasswordResetRequest> requests =
    new CopyOnWriteArrayList<PasswordResetRequest>()
    
  private PasswordResetRequestCache() {}
  
  /**
   * Add the passed request, removing any requests located for the 
   * same user
   * @param request the request to add to the cache
   * @return the added request
   */
  PasswordResetRequest addRequest(PasswordResetRequest request) {
    def matchingRequests = this.requests.findAll { it.name == request.name }
    if (matchingRequests != null && matchingRequests.size() > 0) {
      requests.removeAll(matchingRequests)
    }
    this.requests.add(request)
    request
  }

  /**
   * Retrieves the {@link PasswordResetRequest} with the passed request ID.
   * In the process of locating the applicable request object removes
   * all request objects from the cache that have expired (including any
   * that match the passed request ID).
   * 
   * @param reqId
   * @return the valid {@link PasswordResetRequest} in the cache that 
   * contains the passed request ID
   */
  PasswordResetRequest getRequest(String reqId) {
    Date now = new Date()
    PasswordResetRequest match = null
    List<PasswordResetRequest> expired = []
    
    requests.each {
      if (it.expirationDate <= now) {
        expired.add(it)
      } else
      if (it.requestId == reqId) {
        match = it
      }
    }
    
    requests.removeAll(expired)
    return match
  }
  
  boolean removeRequest(PasswordResetRequest request) {
    removeRequest(request.requestId)
  }
  
  boolean removeRequest(String reqId) {
    def request = getRequest(reqId)
    if (request == null) {
      return false
    }
    
    requests.remove(request)
  }
}
