window.$ = require('jquery');
require('angular');
require('./envContext');
require('./recaptcha');
require('./result');
require('./newPassword');

var module = angular.module("passwordChange", ['envContext', 'recaptcha', 
                                               'result', 'newPassword']);

module.controller("PasswordChangeCtrl", 
                  function($scope, $http, envContextService, RecaptchaFactory, 
                           NewPasswordModalService, ResultStatus) {
  
  var endpoint = envContextService.contextPath() + "/api/freeipa/change";
  
  $scope.submit = function(user) {
    $scope.submitted = true;
    
    if (!$scope.passwordChange.$valid) {
      return;
    }
    
    var recaptcha = new RecaptchaFactory();
    var newPasswordModal = new NewPasswordModalService();
    recaptcha.open().closePromise.then(function(recaptcha) {
      // check if the user cancelled the operation
      if (!recaptcha || !recaptcha.value.response) {
        return;
      }

      newPasswordModal.open().closePromise.then(function(password) {

        // check if the user cancelled the operation
        if (!password || !password.value.new) {
          return;
        }

        $http({
          method: 'POST',
          url: endpoint, 
          data: $.param({
            user: user.name,
            pass: user.password,
            newPass: password.value.new,
            recaptcha_response: recaptcha.value.response
          }),
          headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        })
        .success(function(data, status) {
          var result = {};
          if (data.error) {
            var message = data.message ? data.message : data.error;
            result = {
              success: false,
              message: "Failed to change password: " + message
            };
          } else {
            result = { 
              success: true,
              message: "Successfully changed password"
            };
          }
          
          ResultStatus.setStatus(result);
        });
      });
    });
  };
});

module.directive("passwordChangeDirective", function(envContextService) {
  return {
    restrict: 'AEC',
    templateUrl: envContextService.contextPath() + 
                 '/partials/password-change.html'
  };
});

