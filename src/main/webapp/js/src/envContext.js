require('angular');

/**
 * A tiny module for sharing environment context between modules
 */
var EnvContextApp = angular.module("envContext", []);

// Read the context path from the root element
var ctxPath = angular.element("html").data("context-path");
if (ctxPath === null){
  ctxPath = "/";
}

function EnvContext(){}
EnvContext.prototype.contextPath = function(){
  return ctxPath;
};

EnvContextApp.service('envContextService', EnvContext);
