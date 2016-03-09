//# sourceURL=tabContainer.js

'use strict';

window.smartRApp.directive('tabContainer',
    ['$rootScope', 'smartRUtils', '$timeout', function($rootScope, smartRUtils, $timeout) {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/tabContainer.html',
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
