# freeipa-pwd-portal

A self-service password reset portal for FreeIPA that allows FreeIPA users 
to change and reset their passwords without accessing the FreeIPA instance 
directly.

|CAUTION|
|--------|
|The [Iris dependency](https://github.com/xetus-oss/iris) has not yet been published to a Maven repository. You may need to download and install it in your local Maven repository before building and/or running the Free IPA password portal locally.|

## Quick Start

The quick start requires docker engine and docker-compose. Please note that
the compose file uses `privileged=true` in order to run on mac osx.

1. spin up a local FreeIPA server
   
   > Don't move on to step 2 until the logs indicate the FreeIPA 
     server has finished initializing.
   
   ```bash
   docker-compose up -d
   docker-compose logs -f
   ```

2. ensure freeipa.local.xetus.com is resolvable to your localhost

   For example, you can add something like the following to your 
   `/etc/hosts` file:

   ```
   127.0.0.1   freeipa.local.xetus.com
   ```

3. setup password portal pre-reqs in freeipa server

   The `ipa-volume/pw-portal-freeipa-configurer.sh` script will be mounted 
   to the FreeIPA Docker container created via the `docker-compose` file
   and can be used to conveniently initialize the FreeIPA instance with
   the pre-requisites for the password portal.
   
   At this time the configurer script requires one argument: the password 
   for the FreeIPA instance's admin account. Use the following command to
   run the configurer (replacing "YOUR_HOST_IP" with your host's IP
   address):
   
   ```bash
   docker exec freeipa bash /root/shared/pw-portal-freeipa-configurer.sh -ip YOUR_HOST_IP 'testabc123'
   ```
   _Please see the [documentation for the configurer scripts](server/ipa-volume)
   for more details_

4. locally publish the [iris](https://github.com/xetus-oss/iris) dependency
 
   ```bash
   cd ..
   git clone https://github.com/xetus-oss/iris
   cd iris
   ./gradlew publishToMavenLocal
   cd ../freeipa-pwd-portal
   ```

5. setup and run the server and UI

   You'll need two terminals open. 
   
   In the first terminal, setup the configuration file and start the server:
   
   ```bash
   cp server/config/application-dev.example.yml server/config/application-dev.yml
   ./gradlew server:bootRun
   ```
   
   In the second terminal, setup and start the UI:
   
   ```bash
   cd ui
   npm install
   npm run dev
   ```

   You can then access the web UI at `http://localhost:8080`

6. clean up

   When you're done, make sure to remove docker-compose resources:

   ```bash
   docker-compose rm -s
   ```
   
## Design

The FreeIPA Password Portal is split into two parts:

* [server](./server): the API server, which consumes the FreeIPA 
instance and exposes API endpoints for following the password change
and reset workflows; and

* [ui](./ui): the UI server and app, which includes the configurably 
javascript client and node server to serve the javascript client. 

## Known Development Environment Issues

#### Fail to connect to FreeIPA server after restarting container

This is caused by the `named` service failing to start, which causes the
container's default `ipactl start` command to fail. You can get around 
this by manually starting the FreeIPA services:

```
docker exec freeipa ipactl start --ignore-service-failure
```

# Authentication Stories

The freeipa-pwd-portal offers two main user stories:

1. Password Change (when the user still knows their password)

   In the case of a password change, the user is authenticated using 
   their supplied password.

2. Password Reset (when the user does not know their password or it has 
   expired and needs to be changed administratively)

   In the case of a password reset, the password portal authenticates 
   through it's FreeIPA HOST account (using an HTTP service for that 
   host) using a Kerberos keytab, retrieves the user's information and 
   generates a  password reset email with a secure link back to the 
   portal that will allow the user to reset their password. The email is 
   sent to the email address associated with the supplied uid in the Free 
   IPA instance. 

   Once the user follows the generated link, the password portal uses its 
   administrative access to change the password to a generated value and 
   then immediately changes the password (as the user) to the supplied 
   password. Please see [the documentation for the 
   freeipa-pwd-portal](https://github.com/xetus-oss/freeipa-pwd-portal) 
   for information on creating the HOST account, HTTP service and keytab.