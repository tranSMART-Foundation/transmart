//# sourceURL=smartRApp.js

// this is here because smartR.js is loaded by transmartApp, which hasn't included angular
window.smartRApp = angular.module('smartRApp', []).config([
    function () {

    }]);

console.log('smartRApp is now an angular instance', smartRApp);

