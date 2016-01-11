package com.xetus.freeipa.pwdportal

import groovy.transform.CompileStatic
import groovy.transform.ToString

import com.xetus.freeipa.pwdportal.model.PwPortalUser
import com.xetus.iris.FreeIPAConfig
import com.xetus.iris.model.DefaultFreeIPAResponseModelTypeFactory

@ToString(includeNames = true)
@CompileStatic
class SiteConfig {

  /**
   * A file pointing to the directory containing all of the
   * configruations for this instance. Note that this should
   * be configured using the java environment variable
   * specified by {@link SiteConfigLoader#CONFIG_DIR_VAR}
   */
  File externalConfigDir = null
  
  /**
   * The recatpcha private key to use for recaptcha. If this
   * is missing recaptcha will simply be disabled for the site.
   */
  String recaptchaPrivateKey = null
  
  /**
   * The recaptcha public key to use for recaptcha. This will be
   * passed to the client using a JAX-RS endpoint. If this
   * is missing recaptcha will simply be disabled for the site.
   */
  String recaptchaPublicKey = null
  
  /**
   * The key for the request header from which to extract the remote 
   * user's IP if the password portal is being served from behind a 
   * proxy.
   */
  String xForwardedForHeader = "X-Forwarded-For"
  
  /**
   * If true indicates recaptcha should be disabled for the whole site
   * regardless of whether {@link #recaptchaPrivateKey} and 
   * {@link #recaptchaPublicKey} are specified.
   */
  boolean disableRecaptcha = false
  
  /**
   * @return true if {@link #recaptchaPrivateKey} and {@link 
   * #recaptchaPublicKey} are both set and {@link #disableRecaptcha} is
   * set to false
   */
  boolean isRecaptchaEnabled() {
    return recaptchaPrivateKey != null && recaptchaPublicKey != null &&
           !disableRecaptcha
  }
  
  FreeIPAConfig freeipaConfig = new FreeIPAConfig(
    jaasConfigPath: FreeIPAConfig.class.getResource("/jaas.conf").file,
    typeFactory: new DefaultFreeIPAResponseModelTypeFactory()
                  .registerUserClass(PwPortalUser.class)
  )
  
  /**
   * The time limit in seconds to impose on password reset 
   * requests 
   */
  int passwordResetRequestTimeLimit = (15 * 60)
  
  /**
   * The date format to use when sending emails. Should be a
   * pattern according to {@link java.text.SimpleDateFormat}
   */
  String dateFormat = "hh:mm a zzz 'on' MMMM dd yyyy"
  
  /**
   * The default {@link EmailConfig} object that will be merged against
   * all other email configuration objects using {@link 
   * EmailConfig#merge(EmailConfig)} when attempting to send emails. This
   * is particularly helpful if all emails should be sent from the same
   * host with the same port and from address (and password, if necessary).
   */
  EmailConfig defaultEmailConfig = new EmailConfig(
    smtpHost: "localhost",
    smtpPort: "25",
    smtpFrom: "freeipa-pwd-portal@example.com"
  )
  
  /**
   * The configuration for the password reset email that is sent to users 
   * who have requested a password reset. Available bindings for both the 
   * subject and message are: <ul>
   * 
   *  <li>$name: {@link PasswordResetRequest#getName()}
   *  <li>$requestId: {@link PasswordResetRequest#getRequestId()}
   *  <li>$requestIp: {@link PasswordResetRequest#getRequestIp()}
   *  <li>$requestDate: {@link PasswordResetRequest#getRequestDate()}
   *  <li>$expirationDate: {@link PasswordResetRequest#getExpirationDate()}
   *  <li>$generatedLink: The url generated according to {@link 
   *      FreeIPARestService#generateResetLink(PasswordResetRequest)} that the user
   *      will follow to reset their password
   *      
   * </ul>
   * 
   * Note that dates are formatted according to the dateFormat configuration
   */
  EmailConfig passwordResetEmailConfig = new EmailConfig(
    subjectTemplate: "Free IPA Password Reset",
    messageTemplate: 'Dear $name,\n\n'
      + 'The Free IPA Password Portal received a password reset request '
      + 'for your account at $requestDate. You can follow the link below '
      + 'to fulfill your password reset request; please notify a systems '
      + 'administrator immediately if you did not request a password reset '
      + 'at this time.\n\n'
      + '$generatedLink\n\n'
      + 'This link will remain valid until $expirationDate.\n\n'
      + 'Sincerely,\n\nFree IPA Password Portal'
  )
  
  /**
   * The configuration for the password change email that is sent to any
   * user whose password was changed through the password reset portal.
   * Available bindings for both the subject and message are: <ul>
   * 
   *  <li>$name: The user's LDAP name
   *  <li>$date: The date the password was changed
   * 
   * </ul>
   */
  EmailConfig passwordChangeEmailConfig = new EmailConfig(
    subjectTemplate: "Free IPA Password Change",
    messageTemplate: 'Dear $name,\n\n'
      + 'Your password was changed using the FreeIPA Password '
      + 'portal at $date. Please contact a system administrator '
      + 'immediately if you did not change your password at this '
      + 'time.\n\n'
      + 'Sincerely,\n\nFree IPA Password Portal'
  )
  
  void freeipaConfig(@DelegatesTo(FreeIPAConfig) Closure cl) {
    cl.delegate = freeipaConfig
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  void defaultEmailConfig(@DelegatesTo(EmailConfig) Closure cl) {
    cl.delegate = defaultEmailConfig
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  void passwordResetEmailConfig(@DelegatesTo(EmailConfig) Closure cl) {
    cl.delegate = passwordResetEmailConfig
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }
  
  void passwordChangeEmailConfig(@DelegatesTo(EmailConfig) Closure cl) {
    cl.delegate = passwordChangeEmailConfig
    cl.resolveStrategy = Closure.DELEGATE_FIRST
    cl()
  }

}
