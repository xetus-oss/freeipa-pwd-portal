window.$ = require('jquery');
require('angular');
require('./envContext');
require('./recaptcha');
require('./result');

var module = angular.module("passwordResetRequest", ['envContext', 'recaptcha', 'result']);

module.controller("PasswordResetRequestCtrl", 
    function($scope, $http, envContextService, RecaptchaFactory, ResultStatus) {
 
  var endpoint = envContextService.contextPath() + "/api/freeipa/resetrequest";

  $scope.submit = function(user) {
    $scope.submitted = true;
    
    if (!$scope.resetRequest.$valid) {
      return;
    }
    
    var recaptcha = new RecaptchaFactory();
    recaptcha.open().closePromise.then(function(recaptcha) {
      // check if the user cancelled the operation
      if (!recaptcha || !recaptcha.value.response) {
        return;
      }
      
      $http({
        method: 'POST',
        url: endpoint, 
        data: $.param({
          user: user.name,
          recaptcha_response: recaptcha.value.response
        }),
        headers: {'Content-Type': 'application/x-www-form-urlencoded'}
      }).success(function(data, status) {
        var result = {};
        if (data.error) {
          result = {
            success: false,
            message: "Failed to issue password reset email: " + data.error
          };
        } else {
          result = { 
            success: true,
            message: "Successfully issued password reset email."
          };
        }
        
        ResultStatus.setStatus(result);
      });
    });
  };
});

module.directive("passwordResetRequestDirective", function(envContextService) {
  return {
    restrict: 'AEC',
    templateUrl: envContextService.contextPath() + 
                 '/partials/password-reset-request.html'
  };
});

