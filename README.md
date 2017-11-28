# freeipa-pwd-portal



## Quick Start

|CAUTION|
|--------|
|The [Iris dependency](https://github.com/xetus-oss/iris) has not yet 
been published to a Maven repository. You may need to download and 
install it in your local Maven repository before building and/or running 
the Free IPA password portal locally.|


The quick start requires docker engine and docker-compose. Please note that
the compose file uses `privileged=true` in order to run on mac osx.

``1. spin up a local FreeIPA server``

> Don't move on to step 2 until the logs indicate the FreeIPA 
server has finished initializing.

```bash
docker-compose up -d
docker-compose logs -f
```

``2. Make sure freeipa.local.xetus.com is resolvable to your localhost``

For example, you can add something like the following to your 
`/etc/hosts` file:

```
127.0.0.1   freeipa.local.xxetus.com
```

``3. setup password portal pre-reqs in freeipa server``

The `ipa-volume/pw-portal-freeipa-configurer.sh` script will be mounted 
to the FreeIPA Docker container created via the `docker-compose` file
and can be used to conveniently initialize the FreeIPA instance with
the pre-requisites for the password portal.

At this time the configurer script requires one argument; the password 
for the FreeIPA instance's admin account.

```bash
docker exec freeipa bash /root/shared/pw-portal-freeipa-configurer.sh 'testabc123'
```

> The configurer scripts are parameterized and configured by default to
work with this project's development environment.

``3. locally publish the [iris](https://github.com/xetus-oss/iris) dependency``
 
```bash
cd ..
git clone https://github.com/xetus-oss/iris
cd iris
./gradlew publishToMavenLocal
cd ../freeipa-pwd-portal
```

``4. setup and run the server and UI``

You'll need two terminals open. 

In the first terminal, setup the configuration file and start the server:

```bash
cp server/config/application-dev.example.yml server/config/application-dev.yml
./gradlew server:bootRun
```

In the second terminal, setup and start the UI:

```bash
cd ui;
npm install;
npm run dev
```

You can then access the web UI at `http://localhost:8080`

## Design

The FreeIPA Password Portal is split into two parts:

* [server](./server): the API server, which consumes the FreeIPA 
instance and exposes API endpoints for following the password change
and reset workflows; and
* [ui](./ui): the UI server and app, which includes the configurably 
javascript client and node server to serve the javascript client. 

## Known Issues

``Fail to connect to FreeIPA server after restarting container``

This is caused by the `named` service failing to start, which causes the
container's default `ipactl start` command to fail. You can get around 
this by manually starting the FreeIPA services:

```
docker exec freeipa ipactl start --ignore-service-failure
```