
smartRApp.directive('conceptBox', function() {
    return {
        restrict: 'E',
        replace: true,
        scope: {
            conceptGroup: '='
        },
        template: '<div class="queryGroupIncludeSmall"></div>',
        controller: function($scope) {
            //$scope.conceptBoxes[$scope.conceptGroup] = [];
        },
        link: function(scope, elements, attrs) {

            // activate drag & drop for template element
            scope.$evalAsync(function() {
                var div = Ext.get(elements[0]);
                var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
                dtgI.notifyDrop = dropOntoCategorySelection;
            });

            scope.$watch(
                function() { return elements[0].childNodes.length; },
                function() { console.log(elements[0]); scope.conceptBoxes[scope.conceptGroup] = getConcepts(elements[0]); }
            );
        }
    };
});
