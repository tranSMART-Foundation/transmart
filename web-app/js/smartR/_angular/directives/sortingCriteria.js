//# sourceURL=sortingCriteria.js

'use strict';

window.smartRApp.directive('sortingCriteria', [
    '$rootScope',
    'smartRUtils',
    function($rootScope, smartRUtils) {
        return {
            restrict: 'E',
            scope: {
                criteria : '=',
                samples: '='
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/sortingCriteria.html',
            link: function(scope) {
                scope.subsets = smartRUtils.countCohorts();
            }
        };
    }
]);
