# FreeIPA Password Portal
A self-service password portal webapp that allows users in a Free IPA installation to change and reset their password without needing to access the Free IPA instance directly.

|CAUTION!|
|--------|
|The Iris dependency has not yet been published to a Maven repository. You may need to download and install it in your local Maven repository before building and/or running the Free IPA password portal locally.|

## Overview
While Free IPA exposes a beautiful UI for both account administration and account self-service, this web portal is intended to handle scenarios where an external password self-service web portal is required. Please note that this is in no way intended as a replacement for Free IPA.

In order to communicate with the Free IPA instance, this web portal uses Free IPA's JSON RPC API. Configuring the webapp to ensure successful authenticated communication with the Free IPA instance's JSON RPC API is done using Kerberos and JAAS and, to support password resets for Free IPA users in the admin group, the application of an LDIF to the Free IPA's LDAP backend. Please read on for details on how to correctly configure the password portal.

## Configuration
Configuration for the site is split into two sections. The first is configurations for the site while the second is configuration for locating and authenticating against the FreeIPA instance.

### Site
Configuration for the site is executed using groovy configuration files. An example configuraiton file is located in the source at src/main/resources/defaultconfig.groovy, the contents of which are duplicated here for convenience:

```groovy
recaptchaPrivateKey = "YOUR_GOOGLE_RECAPTCHA_PRIVATE_KEY_HERE"
recaptchaPublicKey = "YOUR_GOOGLE_RECAPTCHA_PUBLIC_KEY_HERE"

/*
 * Please see the Iris project for more details on
 * configuring the FreeIPAConfig object.
 */
freeipaConfig {
  hostname = "freeipa.example.com"
  realm = "EXAMPLE.COM"
  krb5ConfigPath = "/etc/krb/krb5.conf"
  jaasConfigPath = "/etc/krb/jaas.conf"
}

defaultEmailConfig {
  smtpHost = "smtp.example.com"
  smtpPort = "25"
  smtpFrom = "freeipa-pwd-portal@example.com"
}
```
The following fields can be configured in the configuration file:

* __externalConfigDir__: an absolute path pointing to a directory containing the siteconfig.groovy configruation file. This can be specified using the "com.xetus.freeipa-pwd-portal.config" environment variable.
* __recaptchaPrivateKey__: the Google ReCaptcha private key to use.
* __recaptchaPublicKey__: the Google ReCaptcha public key to use.
* __disableRecaptcha__: whether ReCaptcha should be disabled on the site.
* __freeipaConfig__: accepts a closure. See the [Iris github](https://github.com/xetus-oss/iris) for fields that can be configured.
* __defaultEmailConfig__: Accepts a closure. The general email configurations for the project from which all other email configurations will inherit. This is useful mainly for configuring a common SMTP host, port, fromAddress (and password, if applicable). Configurable fields: 
* * __smtpHost__: The SMTP host from which the emails should be sent.
* * __smtpPort__: The port through which to connect to the SMTP host.
* * __smtpFrom__: The address from which the emails should be sent.
* * __smtpUser__: The username with which to authenticate against the SMTP host. Optional; the value for smtpFrom will be used if smtpPass is specified and no smtpUser is specified.
* * __smtpPass__: The password with which to authenticate against the SMTP host. If not specified, authentication will not be attempted against the SMTP host.
* * __subjectTemplate__: The template to use for generating the subject for the email. Other than being inherited by the _passwordResetEmailConfig_ and _passwordChangeEmailConfig_ options if they don't define a value this should have no effect when specified for the _defaultEmailConfig_.
* * __messageTemplate__: The template to use for generating the subject for the email. Other than being inherited by the _passwordResetEmailConfig_ and _passwordChangeEmailConfig_ options if they don't define a value this should have no effect when specified for the _defaultEmailConfig_.
* __passwordResetEmailConfig__: accepts a closure. Defines the email configurations for the email to send to users who request a password reset email, with the same available configurable fields as defaultEmailConfig. See the javadoc comments in SiteConfig for variables that are available in the subjectTemplate and messageTemplate bindings.
* __passwordChangeEmailConfig__: accepts a closure. Defines the email configurations for the email to send to users when their password has been changed through the Free IPA Password Portal. Same avaialble configurable fields as the defaultEmailConfig and passwordResetEmailConfig, but with different bindings available; see the javadoc comments in Siteconfig for variables that are available in the subjectTemplate and messageTemplate bindings.

### Authentication
Two authentication mechanisms are used to interact with the Free IPA instance's JSON RPC API: user credentials, in the case of a password change, and Kerberos authentication using a host account. Both authentication mechanisms require a valid Kerberos configuration on the host system. Because authentication is really handled by the [Iris library](https://github.com/xetus-oss/iris), please visit the [Iris github](https://github.com/xetus-oss/iris) for details on properly configuring JAAS and Kerberos on the host system. Iris configurations are consumed by the Free IPA Password Portal using the FreeIPA Password Portal's freeipaConfig configuration value.

Password changes are executed by establishing a session using the Iris project's FreeIPAAuthenticationManager#getSessionClient() method while password reset requests and fulfillments are handled using the FreeIPAAuthenticationManager#getKerberosClient() method.
