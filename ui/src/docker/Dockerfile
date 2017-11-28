FROM node:6.10.3-alpine

ENV HOME=/home/node

COPY package.json $HOME/freeipa-pwd-portal/
RUN chown -R node:node $HOME/*

USER node
WORKDIR $HOME/freeipa-pwd-portal
RUN npm install --only=prod

USER root
COPY . $HOME/freeipa-pwd-portal
RUN chown -R node:node $HOME/*
USER node

ENTRYPOINT ["node", "server.js"]