# freeipa-nginx

The freeipa-nginx docker container is an intermediary proxy
between a development (or testing) environment and the docker-compose 
FreeIPA instance.

## Why?

There are a few reasons (for development environments) that setting
up a FreeIPA proxy makes sense:

1. The default FreeIPA Apache configuration enforces UI redirects to
   the configured hostname (and default port). A proxy opens up the 
   ability to access the FreeIPA instance using different hostnames
   and ports than originally configured.
2. FreeIPA doesn't expose easy configuraiton of which services are
   exposed on which ports. Using a proxy means we can change the 
   exposed ports.

   Note that FreeIPA forces redirects to both the configured hostname
   and port, which is why just binding the FreeIPA container to custom
   host ports isn't adequate.

## Generating the nginx certificate

Executed from the root of the freeipa-pwd-portal project:

```
openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout server/nginx/nginx.key -out server/nginx/nginx.crt
```
