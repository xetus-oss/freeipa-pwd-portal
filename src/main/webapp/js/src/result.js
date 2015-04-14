require('angular');

var module = angular.module("result", []);

module.controller("ResultCtrl", function($scope, $location, ResultStatus) {
  $scope.status = ResultStatus.getStatus();
  
  /*
   * Return the user to the home page if there is no result to display
   */
  if (!$scope.status || !$scope.status.message && $scope.status.success == null) {
    $location.path('/');
  }
  
  $scope.returnToPortal = function() {
    $location.path('/');
  };
});

module.service("ResultStatus", function($location) {
  
  var status = {};
  
  this.setStatus = function(result) {
    
    if (result === null) {
      result = {};
    }
    
    if (angular.isString(result)) {
      result = { message: result };
    }
    
    if (result === true || result === false) {
      result = { success: result };
    }
    
    status = result;
    $location.path('/result');
  };
  
  this.getStatus = function() {
    return status;
  };
  
  this.getStatusMessage = function() {
    return status.message;
  };
  
  this.isSuccess = function() {
    return status.success;
  };
  
});