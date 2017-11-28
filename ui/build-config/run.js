'use strict';
const path = require('path');
const http = require('http');
const express = require('express');
const Webpack = require('webpack');
const history = require('connect-history-api-fallback');
const WebpackDevServer = require('webpack-dev-server');
const webpackDevMiddleware = require('webpack-dev-middleware');
const webpackHotMiddleware = require('webpack-hot-middleware');
const webpackHotServerMiddleware = require('webpack-hot-server-middleware');
const config = require('../config')
const devConfig = require('../config/dev.json');
const clientConfig = require('../build-config/webpack.dev.conf');
const serverConfig = require('../build-config/webpack.server.conf');

process.env.CONFIG_FILE = ['./config/dev.json', './config/dev.override.json']
process.env.NO_START = true;

/**
* The dev-server Express application, which allows us to work on either the UI
* client or UI server simultaneously and get the benefits of live reload and
* hot swapping in both applications. In order to achieve this, the app will:
*
* 1. use the webpack-dev-middleware to serve the UI client bundle from memory.
* The webpack-dev-middleware (and default webpack-dev-server, which this
* dev-server is replacing) serves the bundle from memory for performance
* reasons: DON'T LOOK FOR COMPILED CODE ON YOUR FILE SYSTEM WHEN USING THIS!
* It won't be there.
*
* 2. use the webpack-hot-server-middleware to serve the UI server bundle from
* memory. Our UI server is really just a static server that exposes an endpoint
* for runtime configuration, so nothing fancy.
*
* 3. because the webpack-dev-middleware serves the UI client bundle from
* memory, we'll have to mimic a bit of the UI server functionality in this
* dev-server, since the UI server will be incapable of extracting the UI
* client bundle from the webpack-dev-middleware memory. That's done using the
* connect-history-api-fallback which routes all extensionless requests
* accepting HTML or XML content to /index.html (i.e. -- a Single Page
* Application).
*/
const app = express();

/**
 * Add the requisite webpack-hot-middleware entry to the client config entry
 * array
 * 
 * TODO: figure out why HMR doesn't work!
 */
// clientConfig.entry['hot-middleware'] = 'webpack-hot-middleware/client?name=client';

/**
* Combine the client and server config. While this may seem confusing, this is
* required by the webpack-hot-server-middleware so that it can manage hot
* swapping the server on changes
*/
clientConfig.name = "client";
serverConfig.name = "server";
let combinedConfig = [clientConfig, serverConfig];
combinedConfig.forEach((config) => config.bail = false);

/**
* Compile the client webpack configuration. This gets used in a few places:
*
* 1. the webpack-dev-middleware
* 2. the webpack-hot-middleware
*/
let compiler = Webpack(combinedConfig);

/**
* Create the dev middleware, with custom configurations
*/
let devMiddleware = webpackDevMiddleware(compiler, clientConfig.devServer);

/**
* Register the dev middleware, which will handle compiling and bundling the
* client and server code.
*
* Additionally use the connect-history-api-fallback to serve the
* webpack-dev-middleware in-memory /index.html file for all extensionless,
* non-API requests. See the following for info:
*
* https://github.com/webpack/webpack-dev-middleware/issues/88#issuecomment-252048006
*/
app.use(devMiddleware);
app.use(history());
app.use(devMiddleware);

/*
 * TODO: figure out why vue HMR isn't behaving. This isn't a show stopper, but HMR
 * sure is nice.
 *
 * app.use(webpackHotMiddleware(compiler.compilers.find(c => c.name === 'client')));
 */

app.use(webpackHotServerMiddleware(compiler));

/**
* Run the app
*/
app.listen(devConfig.server.port).address();