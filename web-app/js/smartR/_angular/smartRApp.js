//# sourceURL=smartRApp.js

window.smartRApp = angular.module('smartRApp', ['ngRoute', 'door3.css'])
    .config([function() {
        // app providers config here
    }])
    .run(function($rootScope, $http) {
        // get plugin context path and put it in root scope
        $http.get(pageInfo.basePath + '/SmartR/smartRContextPath').then(
            function(d) { $rootScope.smartRPath = d.data; },
            function(msg) { console.error(msg); }
    )});
