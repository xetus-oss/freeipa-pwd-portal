// ensure Angular picks up jQuery
// TODO: remove the dependency on jQuery and just use jqLite
jQuery = require('jquery');
require("angular");
require("angular-route");
require("./passwordChange");
require("./passwordResetRequest");
require("./passwordReset");

var app = angular.module("pwdPortalApp", ['ngRoute', 'passwordChange', 
                                          'passwordResetRequest', 'passwordReset']);

app.config(['$routeProvider', function($routeProvider) {
  $routeProvider
    
    .when('/', {
      templateUrl: 'partials/password-options.html'
    })
    
    .when('/change', {
      templateUrl: 'partials/password-change.html'
    })
    
    .when('/reset-request', {
      templateUrl: 'partials/password-reset-request.html'
    })
    
    .when('/result', {
      templateUrl: 'partials/result.html'
    })
    
    .otherwise({
      redirectTo: '/'
    });
}]);