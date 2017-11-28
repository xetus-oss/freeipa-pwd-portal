FROM openjdk:8u111-alpine
MAINTAINER Theo Meneau <tmeneau@xetus.com>

RUN mkdir -p /freeipa-pwd-portal/config

#
# Add the bootstrap script
#
ADD src/main/docker/bootscript /freeipa-pwd-portal/bootscript
RUN chmod 755 /freeipa-pwd-portal/bootscript

#
# Add the server jar into the image
#
ADD build/libs/server-*.jar /freeipa-pwd-portal/server.jar

#
# Expose port 443
#
EXPOSE 443/tcp

ENTRYPOINT /freeipa-pwd-portal/bootscript