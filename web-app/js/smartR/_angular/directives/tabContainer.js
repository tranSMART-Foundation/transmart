
smartRApp.directive('tabContainer', ['smartRUtils', function(smartRUtils) {

    return {
        restrict: 'E',
        //template: '<div id="heim-tabs"></div>',
        controller: function($scope) {
            $scope.tabs = [];
            this.addTab = function(name) {
                $scope.tabs.push(name);
            };
        },
        link: function(scope, element) {
            scope.$evalAsync(function() {
                scope.tabs.each(function(tabName) {
                   element[0].innerHTML += '<li class="heim-tab"><a href="#fragment-' + smartRUtils.makeSafeForCSS(tabName) + '"><span>' + tabName + '</span></a></li>';
                });
            });
        }
    };

}]);
