//# sourceURL=smartRApp.js

// this is here because smartR.js is loaded by transmartApp, which hasn't included angular
window.smartRApp = angular.module('smartRApp', [])
    .config([function () {
        // app providers config here
    }])
    .run(function ($rootScope, $http) {
        // get plugin context path and put it in root scope
        $http.get(pageInfo.basePath + '/SmartR/smartRContextPath').then(
            function (d) {
                $rootScope.smartRPath = d.data;
            },
            function (msg) { console.error(msg);}
        );
    });

console.log('smartRApp is now an angular instance', smartRApp);

