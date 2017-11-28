'use strict'
const fs = require('fs');
const path = require('path');
const webpack = require('webpack');
const utils = require('./utils');
const config = require('../config');
const envConfig = require('../config/dev.json');
const nodeModules = {};


/*
* stolen from http://jlongster.com/Backend-Apps-with-Webpack--Part-I
*/
fs.readdirSync('node_modules')
            .filter((x) => ['.bin'].indexOf(x) === -1 )
            .forEach((mod) => nodeModules[mod] = 'commonjs ' + mod );

module.exports = {
  resolve: { extensions: ['.js'] },

  entry: utils.resolve('src/server/server.js'),

  output: {
    path: utils.resolve('dist'),
    filename: 'server.js',
    /*
     * this is required by the webpack-hot-server-middleware
     */
    libraryTarget: 'commonjs2'
  },

  externals: nodeModules,

  module: {
    rules: [
      ...(config.dev.useEslint? [{
        test: /\.(js)$/,
        loader: 'eslint-loader',
        enforce: 'pre',
        include: [utils.resolve('src/server')],
        options: {
          formatter: require('eslint-friendly-formatter'),
          emitWarning: !config.dev.showEslintErrorsInOverlay
        }
      }] : []),
      {
        test: /\.js$/,
        loader: 'babel-loader',
        include: [utils.resolve('src/server')]
      }
    ]
  },

  plugins: [
    new webpack.DefinePlugin({
      'process.env': {
        'NODE_NEV': JSON.stringify(process.env.NODE_ENV)
      }
    })
  ],

  stats: {
    chunks: true,
  },

  node: {
    global: true,
    process: true,
    Buffer: true,
    __filename: false,
    __dirname: false,
    setImmediate: true
  },

  target: 'node'
};
