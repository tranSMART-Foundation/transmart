//# sourceURL=sortingCriteria.js

'use strict';

window.smartRApp.directive('sortingCriteria', [
    '$rootScope',
    function($rootScope) {
        return {
            restrict: 'E',
            scope: {
                criteria : '=',
                subsets : '=',
                samples: '='
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/sortingCriteria.html'
        };
    }
]);
