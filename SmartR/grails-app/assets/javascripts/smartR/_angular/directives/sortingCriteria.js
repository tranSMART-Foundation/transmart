//# sourceURL=sortingCriteria.js

'use strict';
// console.log("initialize Directive sortingCriteria.js");

window.smartRApp.directive('sortingCriteria', [
    '$rootScope',
    function($rootScope) {
        return {
            restrict: 'E',
            scope: {
                criteria: '=',
                samples:  '=',
                subsets:  '='
            },
            templateUrl: $rootScope.smartRPath +  '/assets/smartR/_angular/templates/sortingCriteria.html'
        };
    }
]);
