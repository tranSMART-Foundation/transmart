//# sourceURL=summaryStatistics.js

'use strict';

window.smartRApp.directive('summaryStats', ['rServeService', function(rServeService) {
        return {
            restrict: 'E',
            scope: {
                data: '='
            },
            templateUrl: '<div>__THIS_IS_TEMPLATE__</div>', // TODO to use external template
            link: function(scope, element) {

            }
        }
}]);
