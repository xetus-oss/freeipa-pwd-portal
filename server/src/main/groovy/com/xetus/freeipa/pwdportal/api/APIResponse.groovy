package com.xetus.freeipa.pwdportal.api;

import groovy.transform.CompileStatic

/**
 * A tiny POJO to pass along user-friendly messages to the client. 
 */
@CompileStatic
public class APIResponse {
  public static APIResponse invalidRecaptcha() {
    return new APIResponse(
      message: "Invalid Re-Captcha response",
      help: "Please enter and re-submit your information to try again."
    );
  }
  
  public static APIResponse invalidCredentials() {
    return new APIResponse(
      message: "Invalid credentials",
      help: "Please verify that your credentials are entered correctly " +
            "and try again."
    );
  }
  
  public static APIResponse invalidResetId() {
    return new APIResponse(
      message: "Invalid or expired reset request ID",
      help: "Please request a new password reset email to reset your password."
    );
  }
  
  public static APIResponse lockedAccount() {
    return new APIResponse(
      message: "Locked account",
      help: "Please try again later or request a password reset."
    );
  }
  
  
  public static APIResponse passwordExpired() {
    return new APIResponse(
      message: "Password expired",
      help: "Please reset your password."
    );
  }
  
  public static APIResponse policyViolation(List<String> violations) {
    return new APIResponse(
      message: "New Password violates your FreeIPA account's password policy",
      help: "Please select a new password that meets your FreeIPA " +
            "account's password policy.",
      data: violations
    );
  }
  
  public static APIResponse serverError() {
    return new APIResponse(
      message: "Server error",
      help: "Please contact your FreeIPA password portal administrator " +
            "if the problem persists"
    )
  }
  
  public static APIResponse 
  
  public APIResponse() {}
  public APIResponse(String message) {
    this.message = message;
  }
  
  String message;
  String help;
  Object data;
}