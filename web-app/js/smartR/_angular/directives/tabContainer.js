
smartRApp.directive('tabContainer', ['smartRUtils', function(smartRUtils) {

    return {
        restrict: 'E',
        template: '<div id="heim-tabs" ng-transclude></div>',
        transclude: true,
        controller: function($scope) {
            $scope.tabs = [];
            this.addTab = function(name) {
                $scope.tabs.push(name);
            };
        },
        link: function(scope, element) {
            var template = element.children()[0];

            scope.$evalAsync(function() {
                scope.tabs.each(function(tabName) {
                    template.innerHTML += '<li class="heim-tab">' +
                        '<a href="#fragment-' + smartRUtils.makeSafeForCSS(tabName) + '">' +
                        '<span>' + tabName + '</span>' +
                        '</a></li>';
                });

                $(template).tabs();
            });
        }
    };

}]);
