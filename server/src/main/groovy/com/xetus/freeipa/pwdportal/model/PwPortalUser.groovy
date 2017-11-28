package com.xetus.freeipa.pwdportal.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.xetus.oss.iris.jackson.databind.ListFlatteningDeserializer
import com.xetus.oss.iris.model.freeipa.account.User

import groovy.transform.CompileStatic

@CompileStatic
public class PwPortalUser extends User {
  @JsonDeserialize(using = ListFlatteningDeserializer)
  @JsonProperty("mail")
  String email
}
