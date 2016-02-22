
window.smartRApp.directive('tabContainer', ['smartRUtils', '$timeout', function(smartRUtils, $timeout) {
    return {
        restrict: 'E',
        transclude: true,
        template: '<div id="heim-tabs" style="margin-top: 25px;"> <ul><li class="heim-tab" ng-repeat="tab in tabs">' +
                      '<a href="#{{tab.id}}">{{tab.name}}</a>' +
                  '</li></ul>' +
                  '<ng-transclude-replace></ng-transclude-replace></div>',
        controller: function($scope) {
            $scope.tabs = [];
            this.addTab = function(name) {
                $scope.tabs.push(name);
            };
        },
        link: function(scope, element) {
            $timeout(function() { // init jQuery UI tabs after DOM has rendered
                $('#heim-tabs').tabs();
            });
        }
    };
}]);
