import * as log4js from 'log4js';
import * as fs from 'fs';
import { config, deferredLogs } from './config';

let configFileExists = config.has('logging.config') && 
                       fs.existsSync(config.get('logging.config'))
if (configFileExists) {
  let logConfig = config.get('logging.config');
  let params = {};
  if (config.has('logging.reloadSecs')) {
    params = config.get('logging.reloadSecs');
  }
  log4js.configure(logConfig, params);
}

let logger = log4js.getLogger();
if (configFileExists) {
  logger.info("Loaded log4js configuration from: " + config.get('logging.config'));
} else {
  logger.level = 'info';
  logger.info('Loaded default log4js configuration');
}

deferredLogs.debug.forEach((msg) => logger.debug(msg));
deferredLogs.info.forEach((msg) => logger.info(msg));
deferredLogs.warn.forEach((msg) => logger.warn(msg));

export default logger;