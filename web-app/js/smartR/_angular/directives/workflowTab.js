
smartRApp.directive('workflowTab', ['smartRUtils', function(smartRUtils) {

    return {
        restrict: 'E',
        scope: {
            tabName: '@'
        },
        require: '^tabContainer',
        //template: '<div></div>',
        link: function(scope, element, attrs, tabContainerCtrl) {
            scope.$eval(function() {
                tabContainerCtrl.addTab(scope.tabName);
                element[0].id = smartRUtils.makeSafeForCSS(scope.tabName)
            });
        }
    };

}]);
