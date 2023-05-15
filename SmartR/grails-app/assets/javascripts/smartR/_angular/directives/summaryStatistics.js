//# sourceURL=summaryStatistics.js

'use strict';

// console.log("initialize Directive summaryStatistics.js");

window.smartRApp.directive('summaryStats', [
    '$rootScope',
    function($rootScope) {
	console.log("execute Directive summaryStats");
        return {
            restrict: 'E',
            scope: {
                summaryData: '='
            },
            templateUrl: $rootScope.smartRPath +  '/assets/smartR/_angular/templates/summaryStatistics.html'
        };
    }
]);
