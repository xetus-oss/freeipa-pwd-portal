# freeipa-pwd-portal UI

## Overview

The UIa app and server for the FreeIPA Password Portal.

## Quick Start

1. Install all the dependencies

    ```bash
    ./gradlew ui:setup
    ```

2. Run the app

    ```bash
    ./gradlew ui:run
    ```

## Building

```bash
./gradlew ui:build
```

The distributable will be bundled into `ui/dist/`.

## Configuring

Configurations are exposed using [node-convict](https://github.com/mozilla/node-convict)
and [log4js](https://github.com/log4js-node/log4js-node). To add your 
own override configurations:

```bash
cp config/dev-override.example.json dev-override.json
cp config/log4js.example.json log4js.json
```

## Docker

A command like the following can be used to run the docker container:

```bash
docker run --name portal-ui -d \
           -h freeipa-pwd-portal-ui.local.xetus.com \
           -p 8080:8080 \
           -v $PWD/ui/config:/home/node/freeipa-pwd-portal/config \
           xetusoss/freeipa-pwd-portal-ui \
              --config-file ./config/dev.json \
              --config-file ./config/dev.override.json \
              --config-file ./config/container-override.json 
```

The convict and log4js configuration files should be mounted into the 
container, and should be referenced with their appropriate container paths
via the --config-file command line argument. Alternatively, you can specify
all the configuration options via command line arguments or environment 
variables.