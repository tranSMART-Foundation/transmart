//# sourceURL=sortingCriteria.js

window.smartRApp.directive('sortingCriteria', ['$rootScope', 'processService', function($rootScope, processService) {
    return {
        restrict: 'E',
        scope: {
            criteria : '=',
            subsets : '='
        },
        templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/sortingCriteria.html',
        link: function(scope, element, attrs) {

            processService.registerComponent(scope, 'sortingCriteria');

        }
    }
}]);
