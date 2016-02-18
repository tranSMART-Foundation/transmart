//# sourceURL=summaryStatistics.js

'use strict';

window.smartRApp.directive('summaryStats', ['rServeService', '$rootScope',  function(rServeService, $rootScope) {
        return {
            restrict: 'E',
            scope: {
                summaryData: '='
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/summary.html',
            link: function(scope, element, attrs) {

            }
        }
}]);
