//# sourceURL=tabContainer.js

'use strict';
console.log("initialize Directive tabContainer.js");

window.smartRApp.directive('tabContainer',
    ['$rootScope', 'smartRUtils', '$timeout', function($rootScope, smartRUtils, $timeout) {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: $rootScope.smartRPath +  '/assets/smartR/_angular/templates/tabContainer.html',
            controller: ['$scope', function($scope) {
                $scope.tabs = [];
                this.addTab = function(tab) {
                    $scope.tabs.push(tab);
                };
            }],
            link: function() {
                $timeout(function() { // init jQuery UI tabs after DOM has rendered
                    $('#heim-tabs').tabs();
                });
            }
        };
    }]);
