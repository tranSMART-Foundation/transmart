
window.smartRApp.directive('tabContainer', ['smartRUtils', '$timeout', function(smartRUtils, $timeout) {
    return {
        restrict: 'E',
        transclude: true,
        template: '<ul><li class="heim-tab" ng-repeat="tab in tabs">' +
                      '<a href="#{{tab.id}}">{{tab.name}}</a>' +
                  '</li></ul>' +
                  '<ng-transclude-replace></ng-transclude-replace>',
        controller: function($scope) {
            $scope.tabs = [];
            this.addTab = function(name) {
                $scope.tabs.push(name);
            };
        },
        link: function(scope, element) {
            element[0].id = 'heim-tabs';
            $timeout(function() { // init jQuery UI tabs after DOM has rendered
                $('#heim-tabs').tabs();
            });
        }
    };
}]);
