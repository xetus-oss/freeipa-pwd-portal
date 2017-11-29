package com.xetus.freeipa.pwdportal.ipa.expiration;

import groovy.transform.CompileStatic

@CompileStatic
public class ExpirationAuditResult {
  private List<ExpirationAuditResultEntry> entries;
  private int warningStartDay;
  private Date evaluationDate;
  
  public ExpirationAuditResult(List<ExpirationAuditResultEntry> entries,
                                       int warningStartDay,
                                       Date evaluationDate) {
    this.entries = entries;
    this.warningStartDay = warningStartDay;
    this.evaluationDate = evaluationDate;
  }
  
  public List<ExpirationAuditResultEntry> getEntries() {
    return this.entries;
  }
  
  public int getWarningStartDay() {
    return this.warningStartDay;
  }
  
  public Date getEvaluationDate() {
    return this.evaluationDate;
  }
}
