# configurers

A set of scripts to automate setting up and aggregating the FreeIPA 
password portal pre-requisites from the target FreeIPA instance.

|CAUTION|
|-------|
|These scripts have only been tested in development. Please thoroughly inspect the scripts and their arguments before using in a non-development environment.|

## pw-portal-freeipa-configurer

The top-level script that will execute all of the requisite actions:

* create the password portal host and service and apply the appropriate
roles;
* modify the FreeIPA instance's LDAP schema to allow the password portal 
service to change passwords for administrator accounts; 
* generate the keytab for the password portal account;
* create a java truststore with the FreeIPA instance's SSL certificate
(required by the portal for self-signed certs); and
* create a configured `krb5.conf`.

## create-freeipa-truststore

Downloads the FreeIPA server's certificate and installs it into a Java
truststore

## create-krb5-conf

Generates a simple `krb5.conf` file from the supplied configurations. 
The configuration is created with the parameters required by the 
password portal.