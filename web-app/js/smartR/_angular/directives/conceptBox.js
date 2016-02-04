
smartRApp.directive('conceptBox', [function() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            group: '@'
        },
        template: '<div class="queryGroupIncludeSmall"></div>',
        controller: function($scope) {
            // TODO: Can how to get the CorrelationController scope? $parent doesn't seem optimal
            $scope.$parent.conceptBoxes[$scope.group] = [];
        },
        link: function(scope, element) {
            // activate drag & drop for template element
            scope.$evalAsync(function() {
                var div = Ext.get(element[0]);
                var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
                dtgI.notifyDrop = dropOntoCategorySelection;
            });

            // modify the model when concepts are added or removed
            $scope.$watch(
                function() { return element[0].childNodes.length; },
                function() { scope.$parent.conceptBoxes[scope.group] = getConcepts(element[0]) });
        }
    };
}]);
