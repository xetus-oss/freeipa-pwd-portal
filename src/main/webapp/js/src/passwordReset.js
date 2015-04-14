window.$ = require('jquery');
require('angular');
require('./envContext');
require('./result');
require('./newPassword');

var module = angular.module("passwordReset", ['envContext', 'result', 'newPassword']);

module.directive('passwordResetDirective', 
    function($http, envContextService, ResultStatus, NewPasswordModalService) {
  return {
    restrict: 'AEC',
    link: function(scope, el, attrs) {
      
      var endpoint = envContextService.contextPath() + '/api/freeipa/reset';
      var requestId = attrs.requestId;
      var expired = attrs.requestExpired == "true";
      
      
      if (!requestId) {
        return;
      }
      
      if (expired) {
        ResultStatus.setStatus({
          message: "Password reset request has expired. Please return to the " + 
            "portal and request a new reset email.",
          success: false
        });
      } else {
        var newPasswordModal = new NewPasswordModalService();
        newPasswordModal.open().closePromise.then(function(password) {

          // check if the user cancelled the operation
          if (!password || !password.value.new) {
            return;
          }
          
          $http({
            method: 'POST',
            url: endpoint, 
            data: $.param({
              requestId: requestId,
              newPassword: password.value.new
            }),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
          })
          .success(function(data, status) {
            
            var result = {};
            if (data.error) {
              var message = data.message ? data.message : data.error;
              result = {
                success: false,
                message: "Failed to set password: " + message
              };
            } else {
              result = { 
                success: true,
                message: "Successfully set password"
              };
            }
            
            ResultStatus.setStatus(result);
          });
        });
      }
    }
  };
});