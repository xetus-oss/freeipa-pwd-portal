package com.xetus.freeipa.pwdportal.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.xetus.iris.jackson.databind.ListFlatteningDeserializer
import com.xetus.iris.model.freeipa.account.User

public class PwPortalUser extends User {
  @JsonDeserialize(using = ListFlatteningDeserializer)
  @JsonProperty("mail")
  String email
}
