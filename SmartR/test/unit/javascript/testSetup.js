var pageInfo = {};
pageInfo.basePath = 'http://localhost';

// mockup of smartRApp to avoid GET request before it can be handled by $httpBackend
window.smartRApp = angular.module('smartRApp', ['ngRoute', 'door3.css'])
    .run(function($rootScope) {
    	'use strict';
    	$rootScope.smartRPath = '';
    });
