import * as http from 'http';
import * as express from 'express';
import * as history from 'connect-history-api-fallback';
import * as compression from 'compression';
import config from './config';
import logger from './logger';

let app = express();
app.use(compression());

/*
 * Expose a global window variable with the UI configuration. This allows
 * for runtime configuration of the browser app while taking advantage of
 * the browser pre-loading the configuration.
 */
app.use('/config.js', (req, res) => {
  var uiConfig = { 
    API_BASE: config.get('api.baseUrl'),
    RECAPTCHA_PUBLIC_KEY: config.get('recaptcha.publicKey'),
    RECAPTCHA_ENABLED: config.get('recaptcha.enabled')
  };
  res.send("window.UiConfig=" + JSON.stringify(uiConfig, null, 2) + ";");
});

app.use(express.static(config.get('server.staticRoot')));
app.use(history());
app.use(express.static(config.get('server.staticRoot')));

/*
 * Support not having the server listen. This is useful in
 * development to allow the exported app be used by a dev server
 */
if (!config.get('server.noStart')) {
  let port = config.get('server.port')
  app.listen(port, () =>
    logger.info(
      "FreeIPA Pwd Portal UI server is listening on port ", 
      port
    )
  );
} else {
  logger.warn("Not starting the server due to server.noStart configuration!");
}

export default (stats) => (req, res, next) => {
  logger.debug("Handling request for: ", req.url);
  app.handle(req, res, next);
};

export const server = app;