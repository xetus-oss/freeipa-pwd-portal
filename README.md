# FreeIPA Password Portal
A self-service password portal webapp that allows user accounts in a Free IPA installation to change and reset their passwords without needing to access the Free IPA instance directly.

|CAUTION|
|--------|
|The [Iris dependency](https://github.com/xetus-oss/iris) has not yet been published to a Maven repository. You may need to download and install it in your local Maven repository before building and/or running the Free IPA password portal locally.|

## Overview
While Free IPA exposes a beautiful UI for both account administration and account self-service, this web portal is intended to handle scenarios where a password self-service web portal external to the Free IPA instance is required.

## Quick Start

#### <a name="prerequisites"></a>Pre-Requisites - FreeIPA Instance

The following steps should be taken on the FreeIPA instance with which the freeipa-pwd-portal instance will authenticate. Make sure to change references to `example.com` below to your domain as applicable. _The following steps are also required for deployments using the Docker image_:

1. Open the FreeIPA web UI and authenticate using an account with admin privileges
2. Add the host `freeipa-pwd-portal.example.com` using the FreeIPA web UI. Give it the "User Administrator" role and make sure to configure it's IP in FreeIPA's DNS registry if necessary
3. Add the service `HTTP/freeipa-pwd-portal.example.com`
4. Get a keytab for the host and service by running the following commands from FreeIPA host command line, changing the parameters as applicable. Make sure you have a valid Kerberos session by running 'kinit' first:

	```bash
	ipa-getkeytab -s freeipa.example.com -p host/freeipa-pwd-portal.example.com -k /tmp/freeipa-pwd-portal.example.com.keytab
	ipa-getkeytab -s freeipa.example.com -p HTTP/freeipa-pwd-portal.example.com -k /tmp/freeipa-pwd-portal.example.com.keytab
	```
	c. copy the keytab from `/tmp/freeipa-pwd-portal.example.com.keytab` on the FreeIPA server to `/etc/iris/keytab` on the freeipa-pwd-portal server
	
5. All FreeIPA versions since 2.2 [restrict non-admin users from changing admin passwords](https://fedorahosted.org/freeipa/ticket/2271). To allow the freeipa-pwd-portal to reset admin passwords against accounts in FreeIPA versions greater than this:

	1. Create a `host group` in the FreeIPA instance with the name `pw-reset-portal` and add the freeipa-pwd-portal.example.com host you created in step 2 above as a member
	2. Apply the following ldif to the LDAP directory, modifying all instances of `dc=example,dc=com` to match your basedn:
		
		```
		# Add the ability to change passwords for all accounts (including) admins
		# using this host account
		dn: dc=example,dc=com
		changetype: modify
		add: aci
		aci: (targetattr = "userPassword || krbPrincipalKey || sambaLMPassword || sambaNTPassword || passwordHistory || ipaNTHash")(version 3.0; acl "PWD Portal can write passwords"; allow (add,delete,write) groupdn="ldap:///cn=pw-reset-portal,cn=hostgroups,cn=accounts,dc=example,dc=com";)
		```
		
	3. After creating a file called freeipa-pwd-portal.ldif from the above ldif in your current working directory, an example of the command to run from the FreeIPA server might be:
		
		```bash
		ldapmodify -h freeipa.example.com -x -W \
		  -p 389 \
		  -D "cn=Directory Manager" \
		  -f freeipa-pwd-portal.ldif
		```
		
#### Docker

Using the Docker image is by far the quickest way to get started, although the steps outlined in [FreeIPA Instance deployment requirements](#prerequisites) still need to be completed prior to deployment. See the [docker-freeipa-pwd-portal project's documentation](https://github.com/xetus-oss/docker-freeipa-pwd-portal) for details.

#### WAR

###### Build the WAR From Source (Temporarily required)

The following steps will only be required until the freeipa-pwd-portal and it's iris dependency are published to public artifact repositories.

1. Clone the [Iris dependency](https://github.com/xetus-oss/iris)
2. From the Iris project's root folder, run `./gradlew publishToMavenLocal` to install the jar to your local m2repo
3. Clone the freeipa-pwd-portal project to the same system
4. From the freeipa-pwd-portal's root folder, run `npm install`
5. run `./gradlew war` to build the war file to build/libs/

###### Deployment Configuration

The following steps should be taken on the host system that will run the freeipa-pwd-portal instance. 

1. Modify the following freeipa-pwd-portal siteconfig template as necessary and place the contents in `/etc/freeipa-pwd-portal/siteconig.groovy` (see #configuration for details):

	```groovy
	recaptchaPrivateKey = "GOOGLE_RECAPTCHA_PRIVATE_KEY"
	recaptchaPublicKey = "GOOGLE_RECAPTCHA_PUBLIC_KEY"
	disableRecaptcha = false
	
	/*
	 * Please see the Iris project for more details on
	 * configuring the FreeIPAConfig object.
	 */
	freeipaConfig {
	  hostname = "freeipa.example.com"
	  realm = "EXAMPLE.COM"
	  krb5ConfigPath = "/etc/iris/krb5.conf"
	  jaasConfigPath = "/etc/iris/jaas.conf"
	}
	
	defaultEmailConfig {
	  smtpHost = "smtp.example.com"
	  smtpPort = "25"
	  smtpFrom = "freeipa-pwd-portal@example.com"
	}
	```

2. Modify the following logging configuration to your preference and place the result in `/etc/freeipa-pwd-portal/logback.groovy`:

	```groovy
	import ch.qos.logback.classic.encoder.PatternLayoutEncoder
	import ch.qos.logback.core.rolling.FixedWindowRollingPolicy
	import ch.qos.logback.core.rolling.RollingFileAppender
	import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy
	import ch.qos.logback.core.status.OnConsoleStatusListener
	
	import static ch.qos.logback.classic.Level.*
	
	appender("ROLLING_FILE", RollingFileAppender) {
	    file = "/var/log/freeipa-pwd-portal/application.log"
	    encoder(PatternLayoutEncoder) {
	      pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{req.remoteHost}] %-5level %logger{0} - %msg%n"
	    }
	    triggeringPolicy(SizeBasedTriggeringPolicy) {
	        maxFileSize = '10MB'
	    }
	    rollingPolicy(FixedWindowRollingPolicy) {
	        fileNamePattern = "/var/log/freeipa-pwd-portal/application-%d{yyyyMMdd_hhmmss}.%i.gz"
	        maxIndex = 10
	    }
	}
	
	logger("org.apache.cxf.phase.PhaseInterceptorChain", ERROR)
	logger("com.xetus", INFO, ["ROLLING_FILE"], false)
	root(WARN, ["ROLLING_FILE"])
	```


3. Modify the following krb5 config template as necessary and place the contents in `/etc/iris/krb5.conf` (see [iris](https://github.com/xetus-oss/iris) for details):

	```ini
	[logging]
	 default = FILE:var/logs/krb5.log
	
	[libdefaults]
	 default_realm = EXAMPLE.COM
	 dns_lookup_realm = true
	 dns_lookup_kdc = true
	 ticket_lifetime = 24h
	 forwardable = true
	
	[realms]
	 EXAMPLE.COM = {
	  kdc = freeipa.example.com
	  admin_server = freeipa.example.com
	 }
	
	[domain_realm]
	 example.com = EXAMPLE.COM
	```

3. Modify the following template as necessary and place the contents in `/etc/iris/jaas.conf` (see [iris](https://github.com/xetus-oss/iris) for details):

	```
	com.sun.security.jgss.krb5.initiate {
        com.sun.security.auth.module.Krb5LoginModule required
        doNotPrompt=true
        useKeyTab=true
        keyTab="/etc/iris/keytab"
        storeKey=true
        useTicketCache=true
        principal="host/freeipa-pwd-portal.example.com@EXAMPLE.COM";
	};
	```

4. Import the SSL certificate to the java keystore the freeipa-pwd-portal instance will be using. See [Tomcat's documentation](https://tomcat.apache.org/tomcat-6.0-doc/ssl-howto.html) for details.

6. Start the server on port 443 by running the following command, replacing `$KEYSTORE_PATH`, `$CERT_ALIAS` and `$STORE_AND_CERT_PASS` as necessary. Note that currently the webapp must be run through https:

	```bash
	java -jar freeipa-pwd-portal-1.0-SNAPSHOT.war \
      -p 443 \
      -kf "$KEYSTORE_PATH" \
      -ka "$CERT_ALIAS" \
      -kp "$STORE_AND_CERT_PASS"
	```
	
## Configuration

#### WAR CLI
The WAR CLI exposes a few very simple command line arguments:

* __-p/--port__: the port on which the server should run
* __-kf/--keystore-file__: the absolute path to the keystore from which the SSL certificate should be extracted
* __-kp/--keystore-pass__: the password to the keystore. Note that the password to the certificate must be the same, per Tomcat's requirements
* __-ka/--keystore-alias__: the alias under which the certificate to use is saved in the keystore

#### siteconfig.groovy
Configuration for the site is executed using a groovy configuration file. The following fields can be configured in the siteconfig configuration file:

* __externalConfigDir__: an absolute path pointing to a directory containing the siteconfig.groovy configruation file. This can be specified using the "com.xetus.freeipa-pwd-portal.config" environment variable.
* __recaptchaPrivateKey__: the Google ReCaptcha private key to use.
* __recaptchaPublicKey__: the Google ReCaptcha public key to use.
* __disableRecaptcha__: whether ReCaptcha should be disabled on the site.
* __freeipaConfig__: accepts a closure. See the [iris documentation](https://github.com/xetus-oss/iris) for a listing of fields that can be configured for the FreeIPAConfig object.
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

#### Kerberos (krb5.conf and jaas.conf)

Please see the [iris documentation](https://github.com/xetus-oss/iris) for details on properly configuring the freeipaConfig object and on properly setting up the jaas.conf and krb5.conf (if applicable) configuration files.


## Details

In order to communicate with the Free IPA instance, the freeipa-pwd-portal uses Free IPA's JSON RPC API. However, two authentication mechanisms are used to interact with the Free IPA instance's JSON RPC API: 

1. Credentialed Authentication (Password Change)

	In the case of a password change, the portal authenticates as the the password change is executed as the user. On successful change of the password, the user's email address is retrieved from the FreeIPA instance and an email is sent to that address indicating that the password was changed.
	
2. Kerberos Authentication (Password Reset)

	Because the user is unable to authenticate (due to an expired or forgotten password), the freeipa-pwd-portal:
	1. uses it's Kerberos principal (with _User Administration_ privileges) to retrieve the email corresponding to the supplied username
	2. emails a secure password-reset link to the email (the password-reset link is valid for a configurable 15 minute window)
	3. waits until it receives a request with the valid password-reset ID and a new password to: change the user's password to a secure, randomly generated value; authenticate as the user using the random value; and changes the user's password (as the now-authenticated user) to the supplied new password.

Both authentication mechanisms require a valid Kerberos configuration on the host system. Because authentication is really handled by the [iris library](https://github.com/xetus-oss/iris), please visit that project for more details on configuring JAAS and Kerberos on the host system. Iris configurations are configured using the `freeipaConfig` configuration object.
