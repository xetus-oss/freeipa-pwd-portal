package com.xetus.freeipa.pwdportal.ipa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.xetus.oss.iris.FreeIPAAuthenticationManager;
import com.xetus.oss.iris.FreeIPAClient
import com.xetus.oss.iris.model.DefaultFreeIPAResponseModelTypeFactory
import com.xetus.oss.iris.model.FreeIPAResponseModelTypeFactory

import com.xetus.freeipa.pwdportal.model.PwPortalUser

import groovy.transform.CompileStatic

@Component
@CompileStatic
public class FreeIPAClientService {
  
  FreeIPAAuthenticationManager mgr;
  FreeIPAResponseModelTypeFactory typeFactory = 
      new DefaultFreeIPAResponseModelTypeFactory()
          .registerUserClass(PwPortalUser.class);
  
  @Autowired
  public FreeIPAClientService(FreeIPAAuthenticationManager mgr) {
    this.mgr = mgr;
  }
  
  public FreeIPAClient getKerberosClient() {
    return new FreeIPAClient(this.mgr.getRPCKerberosClient(), typeFactory);
  }
  
  public FreeIPAClient getSessionClient(String user, String pass) {
    return new FreeIPAClient(
      this.mgr.getSessionClient(user, pass),
      typeFactory
    );
  }
  
  public FreeIPAClient resetPassword(String user, String oldPass, String newPass) {
    return new FreeIPAClient(
        this.mgr.resetPassword(user, oldPass, newPass),
        typeFactory
     );
  }
}
