require('angular');
require('ngDialog');
require('./envContext');

var module = angular.module("newPassword", ['ngDialog', 'envContext']);

module.controller('NewPasswordCtrl', function($scope) {
  
  $scope.submit = function(pwd) {
    $scope.submitted = true;
    
    if (!$scope.newPassword.$valid) {
      return;
    }
    
    $scope.closeThisDialog(pwd);
  };
});

module.factory('NewPasswordModalService', function(envContextService, ngDialog) {
  function NewPasswordModalService() {
    this.template = envContextService.contextPath() + 
                    '/partials/new-password.html';
  }
  
  NewPasswordModalService.prototype.open = function() {
    return ngDialog.open({ 
      template: this.template,
      cache: false
    });
  };
  
  return NewPasswordModalService;
});

module.directive('newPasswordDirective', function(envContextService) {
  return {
    retrict: 'AEC',
    templateUrl: envContextService.contextPath() + '/partials/new-password.html'
  };
});

/**
 * Requires that an element matching the configured selector has the
 * same value. The element is re-validated when either the element or
 * matching element's value is changed. Example HTML consuming this
 * directive might look like:
 * 
 * <input ng-model='pass' require-equals='confirm' />
 * <input ng-model='confirm' require-equals='pass' />
 * 
 * @param require-equals-target-attr the attribute on the target 
 * element whose value should match the require-equals parameter. 
 * Defaults to 'ng-model'.
 * @param require-equals the value on the target element to match 
 * in the target element's target attribute.
 */
module.directive('requireEquals', function() {
  return {
    restrict: 'A', // only activate on element attribute
    require: '?ngModel',
    link: function(scope, elem, attrs, ngModel) {

      ngModel.$viewChangeListeners.push(function() { 
        validate(); 
      });
      
      var other = null;
      var validate = function() {
        
        /*
         * Bit of hackery since we can't guarantee that the DOM for the
         * other element has been constructed at the time this link 
         * function executes
         */
        if (!other) {
          
          var attr = attrs.requireEqualsTargetAttr || 'ng-model';
          var otherId = attrs.requireEquals;
          var selector = '[' + attr + '=\"' + otherId + '\"]';
          other = document.querySelector(selector);
          
          /*
           * At this point the consumer must have entered invalid
           * configurations in their HTML
           */
          if (!other) {
            throw new Error("No element matches selector: " + selector);
          }
          
          var otherCtrl = angular.element(other).controller('ngModel');
          otherCtrl.$viewChangeListeners.push(function() { 
            validate(); 
          });
        }

        var val1 = elem[0].value;
        var val2 = other.value;
        
        // Set the validity, letting the 'require' validator handle null
        // values in either
        ngModel.$setValidity('requireEquals',
            !val1 || !val2 || val1 == val2);
      };
    }
  };
});