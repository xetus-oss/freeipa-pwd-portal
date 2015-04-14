require('angular');
require('angular-recaptcha');
require('ngDialog');
require('./envContext');

var module = angular.module("recaptcha", ['ngDialog', 'vcRecaptcha', 
                                          'envContext']);

module.controller('RecaptchaCtrl', function($scope, vcRecaptchaService, 
                                            recaptchaConfig) {
  
  var recaptcha = {
    response: null,
    widgetId: null
  };
  
  $scope.model = {
    key: recaptchaConfig.getPublicKey() 
  };
  
  $scope.setResponse = function(response) {
    recaptcha.response = response;
  };
  
  $scope.setWidgetId = function(widgetId) {
    recaptcha.widgetId = widgetId;
  };
  
  $scope.submit = function() {
    $scope.closeThisDialog(recaptcha);
    vcRecaptchaService.reload($scope.widgetId);
  };
});

module.factory("RecaptchaFactory", function($q, ngDialog, envContextService, 
                                            recaptchaConfig) {
  
  function RecaptchaFactory() {
    this.template = envContextService.contextPath() + '/partials/recaptcha.html';
  }

  /**
   * @return the opened ngDialog object. The ngDialog object contains a
   * closePromise which, upon fulfillment, will return an object with
   * the widget ID and user response that can be used to validate server-side:  
   * 
   *  {
   *    widgetId: <<id>>,
   *    response: <<response>>
   *  }
   *    
   * Example usage would be:
   * 
   * new RecaptchaFactory().open().closePromise.then(function(recaptcha) {
   *  // do something with recaptcha.widgetId and recaptcha.response here
   * });
   * 
   * If the recaptchaConfig provider has been configured to disable Recatpcha
   * the returned closePromise will be immediately resolved with a response
   * of "disabled".
   * 
   * TODO: this is a lazy hack and should be re-thought.
   */
  RecaptchaFactory.prototype.open = function() {
    if (recaptchaConfig.isEnabled()) {
      return ngDialog.open({ 
        template: this.template,
        cache: false
      });
    } else {
      return {
        closePromise: $q(function(resolve, reject) {
          resolve({
            value: { response: "disabled" }
          });
        })
      };
    }
  };
  
  return RecaptchaFactory;
});

/**
 * Provider that retrieves the recaptcha configuration form the API endpoint.
 * This is done to facilitate configuration of the webapp by allowing the 
 * public and private keys to be configured in an external file together.
 */
module.provider('recaptchaConfig', function() {
  var config = null;

  var configure = function(c) {
    config = c;
  }
  
  return {
    $get: ['$http', 'envContextService', function($http, envContextService) {
      var endpoint = envContextService.contextPath() + '/api/config/recaptcha';
      $http.get(endpoint).success(function (result) {
        configure(result)
      });
      return this;
    }],
    isEnabled: function() { return config.enabled; },
    getPublicKey: function() { 
      if (config.enabled) {
        return config.recaptchaPublicKey;
      }
    }
  };
});