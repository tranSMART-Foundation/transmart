//# sourceURL=sortingCriteria.js

'use strict';

window.smartRApp.directive('sortingCriteria', ['$rootScope', function($rootScope) {
    return {
        restrict: 'E',
        scope: {
            criteria : '='
        },
        templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/sortingCriteria.html',
        link: function(scope, element, attrs) {

        }
    }
}]);
