package com.xetus.freeipa.pwdportal.ipa.reset

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.util.concurrent.CopyOnWriteArrayList

import org.springframework.stereotype.Component

/**
 * A simple cache of {@link PasswordResetRequest} objects that
 * enforces expiration and uniqueness of it's contained requests.
 */
@Component
@CompileStatic
class ResetRequestCacheService {

  private List<ResetRequest> requests = new CopyOnWriteArrayList<ResetRequest>()
  
  /**
   * Add the passed request, removing any requests located for the 
   * same user
   * @param request the request to add to the cache
   * @return the added request
   */
  ResetRequest addRequest(ResetRequest request) {
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
   * @param resetId
   * @return the valid {@link PasswordResetRequest} in the cache that 
   * contains the passed request ID
   */
  ResetRequest getRequest(String resetId) {
    Date now = new Date()
    ResetRequest match = null
    List<ResetRequest> expired = []
    
    requests.each {
      if (it.expirationDate <= now) {
        expired.add(it)
      } else
      if (it.resetId == resetId) {
        match = it
      }
    }
    
    requests.removeAll(expired)
    return match
  }
  
  boolean removeRequest(ResetRequest request) {
    removeRequest(request.resetId)
  }
  
  boolean removeRequest(String resetId) {
    def request = getRequest(resetId)
    if (request == null) {
      return false
    }
    
    requests.remove(request)
  }
}
