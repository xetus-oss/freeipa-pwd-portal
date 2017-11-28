# freeipa-pwd-portal server

## Overview

While Free IPA exposes a beautiful UI for both account administration 
and account self-service, this web portal is intended to handle scenarios 
where a password self-service web portal external to the Free IPA 
instance is required.

## <a name="prerequisites"></a>Pre-Requisites - FreeIPA Instance

The following steps should be taken on the FreeIPA instance with which 
the freeipa-pwd-portal instance will authenticate. Make sure to change 
references to `example.com` below to your domain as applicable. _The 
following steps are also required for deployments using the Docker image_:

1. Open the FreeIPA web UI and authenticate using an account with 
   admin privileges
2. Add the host `freeipa-pwd-portal.example.com` using the FreeIPA 
   web UI. Give it the "User Administrator" role and make sure to 
   configure it's IP in FreeIPA's DNS registry if necessary
3. Add the service `HTTP/freeipa-pwd-portal.example.com`
4. Get a keytab for the host and service by running the following 
   commands from FreeIPA host command line, changing the parameters as 
   applicable. Make sure you have a valid Kerberos session by running 
   'kinit' first:

   ```bash
   ipa-getkeytab -s freeipa.example.com -p host/freeipa-pwd-portal.example.com -k /tmp/freeipa-pwd-portal.example.com.keytab
   ipa-getkeytab -s freeipa.example.com -p HTTP/freeipa-pwd-portal.example.com -k /tmp/freeipa-pwd-portal.example.com.keytab
   ```
   Then copy the keytab from `/tmp/freeipa-pwd-portal.example.com.keytab` 
   on the FreeIPA host to `config/freeipa-pwd-portal.example.com.keytab` 
   from the root of the password portal jar's parent directory on the 
   freeipa-pwd-portal host (you can move this later if needed).
  
5. All FreeIPA versions since 2.2 [restrict non-admin users from 
   changing admin passwords](https://fedorahosted.org/freeipa/ticket/2271). 
   To allow the freeipa-pwd-portal to reset admin passwords against 
   accounts in FreeIPA versions greater than this:

   1. Create a `host group` in the FreeIPA instance with the name 
      `pw-reset-portal` and add the freeipa-pwd-portal.example.com host 
      you created in step 2 above as a member
   2. Apply the following ldif to the LDAP directory, modifying all 
      instances of `dc=example,dc=com` to match your basedn:
    
      ```
      # Add the ability to change passwords for all accounts (including) admins
      # using this host account
      dn: dc=example,dc=com
      changetype: modify
      add: aci
      aci: (targetattr = "userPassword || krbPrincipalKey || sambaLMPassword || sambaNTPassword || passwordHistory || ipaNTHash")(version 3.0; acl "PWD Portal can write passwords"; allow (add,delete,write) groupdn="ldap:///cn=pw-reset-portal,cn=hostgroups,cn=accounts,dc=example,dc=com";)
      ```
    
   3. After creating a file called freeipa-pwd-portal.ldif from the above 
      ldif in your current working directory, an example of the command to 
      run from the FreeIPA server might be:
    
      ```bash
      ldapmodify -h freeipa.example.com -x -W \
                 -p 389 \
                 -D "cn=Directory Manager" \
                 -f freeipa-pwd-portal.ldif
      ```

## Deployment Configuration

The following steps should be taken on the host system that will run 
the freeipa-pwd-portal instance. 

1. Create a `config/application.yml` file from the root of the password 
   portal jar's parent directory. You can use [the example dev 
   configuration file](config/application-dev.example.yml) as an example.

  > See https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
  for details on resolution of configuration files 

2. Create a `config/krb5.conf` file from the root of the password portal
   jar's parent directory. You can use the following krb5 config 
   template as an example (see [iris](https://github.com/xetus-oss/iris) 
   for details):

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

3. Import the SSL certificate to the java keystore the freeipa-pwd-portal 
   instance will be using. See [Tomcat's documentation](https://tomcat.apache.org/tomcat-6.0-doc/ssl-howto.html) 
   for details.

4. Start the server:

   ```bash
   java -jar freeipa-pwd-portal-1.0-SNAPSHOT.war
   ```

## Details

In order to communicate with the Free IPA instance, the freeipa-pwd-portal 
uses Free IPA's JSON RPC API. However, two authentication mechanisms are 
used to interact with the Free IPA instance's JSON RPC API: 

1. Credentialed Authentication (Password Change)

   In the case of a password change, the portal authenticates as the the 
   password change is executed as the user. On successful change of the 
   password, the user's email address is retrieved from the FreeIPA 
   instance  and an email is sent to that address indicating that the 
   password was changed.
  
2. Kerberos Authentication (Password Reset)

   Because the user is unable to authenticate (due to an expired or 
   forgotten password), the freeipa-pwd-portal:
  
   1. uses its Kerberos principal (with _User Administration_ 
      privileges) to retrieve the email corresponding to the supplied 
      username
      
   2. emails a secure password-reset link to the email (the password-
      reset link is valid for a configurable 15 minute window)
      
   3. waits until it receives a request with the valid password-reset ID 
      and a new password to: change the user's password to a secure, 
      randomly generated value; authenticate as the user using the random 
      value; and changes the user's password (as the now-authenticated user) 
      to the supplied new password.

Both authentication mechanisms require a valid Kerberos configuration on 
the host system. Because authentication is really handled by the [iris 
library](https://github.com/xetus-oss/iris), please visit that project 
for more details on configuring JAAS and Kerberos on the host system. 
Iris configurations are configured using the `freeipaConfig` 
configuration object.

#### Docker

The FreeIPA PWD Portal server docker container comes with a couple of 
benefits:
 
1. Externalized configuration through the `/freeipa-pwd-portal/config` 
   container path.
   
2. Auto-installation of certificates, including:
   * the Free IPA instance's certificate;
   * the keystore containing the password portal's certificate; or
   * generation of a self-signed certificate and keystore if none is provided


###### Quick Start

If run from the root project directory, the below command will use 
server/config as a mount point and use `server/config/application.yml`
as the configuration file:

```
docker run --name portal-server -d \
           -h freeipa-pwd-portal.example.com \
           -p 443:443 \
           -v $PWD/server/config/:/freeipa-pwd-portal/config \
           xetusoss/freeipa-pwd-portal
```

To see the configurable options, run:

```
docker run --rm xetusoss/freeipa-pwd-portal --help
```

###### Building

You can buid the docker conatiner by running:

```
./gradlew buildDocker
```