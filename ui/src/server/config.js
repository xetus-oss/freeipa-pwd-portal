import * as path from 'path';
import * as fs from 'fs';
import * as convict from 'convict';

const convictConfig = convict({
  configFile: {
    doc: 'Optional path to a configuration file containing the desired configurations',
    format: Array,
    default: ['./config/config.json'],
    env: 'CONFIG_FILE',
    arg: 'config-file'
  },
  api: {
    baseUrl: {
      doc: 'The base URL (including port and app prefix) for the FreeIPA PWD Portal server API',
      format: 'url',
      default: 'http://localhost:8081',
      env: 'API_BASE_URL',
      arg: 'api-base-url'
    }
  },
  recaptcha: {
    enabled: {
      doc: 'Whether ReCAPTCHA is enabled for the deployment. Must match the value configured for the server',
      default: false,
      format: Boolean,
      env: 'RECAPTCHA_ENABLED',
      arg: 'recaptcha-enabled'
    },
    publicKey: {
      doc: 'The ReCAPTCHA public key to use',
      default: null,
      format: String,
      env: 'RECAPTCHA_PUBLIC_KEY',
      args: 'recaptcha-public-key'
    }
  },
  server: {
    staticRoot: {
      doc: 'The path to the directory containing the static client assets to serve',
      default: __dirname,
      format: String,
      env: 'STATIC_ROOT',
      arg: 'static-root'
    },
    port: {
      doc: 'The port on which the UI server should run',
      default: 8080,
      format: 'port',
      env: 'PORT',
      arg: 'port'
    },
    noStart: {
      doc: 'An optional flag to stop the server from starting, mostly for development',
      default: false,
      format: Boolean,
      env: 'NO_START'
    }
  },
  logging: {
    config: {
      doc: 'The path to the log4js configuration file to load',
      default: 'config/log4js.conf',
      format: String,
      env: 'LOG4JS_CONFIG',
      arg: 'log4js-config'
    },
    reloadSecs: {
      doc: 'The optional number of seconds after which log4js should check the config file for changes',
      default: null,
      format: Number,
      env: 'LOG4JS_RELOAD_SECS',
      arg: 'log4js-reload-secs'
    }
  }
});

var configFiles = convictConfig.has('configFile') && 
                  convictConfig.get('configFile');
let logs = { debug: [], info: [], warn: [] };
if (configFiles) {
  configFiles.forEach((f) => {
    f = path.resolve(f);
    if (fs.existsSync(f)) {
      convictConfig.loadFile(f);
      logs.info.push("Loaded configurations from:  " + f);
    } else {
      logs.warn.push("Ignored non-existent config: " + f);
    }
  });
  logs.debug.push(
    "Resolved configruations: " +
    JSON.stringify(convictConfig.getProperties(), null, 4)
  );
}

export default convictConfig;

export const config = convictConfig;
export const deferredLogs = logs;