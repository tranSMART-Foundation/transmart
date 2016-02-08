
smartRApp.directive('workflowTab', ['smartRUtils', function(smartRUtils) {

    return {
        restrict: 'E',
        scope: {
            tabName: '@'
        },
        require: '^tabContainer',
        template: '<div ng-transclude></div>',
        transclude: true,
        link: function(scope, element, attrs, tabContainerCtrl) {
            var template = element.children()[0];

            scope.$eval(function() {
                tabContainerCtrl.addTab(scope.tabName);
                template.id = 'fragment-' + smartRUtils.makeSafeForCSS(scope.tabName)
            });
        }
    };

}]);
