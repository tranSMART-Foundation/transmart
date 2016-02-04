
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

            // modify the model when concepts are added or removed
            scope.$watch(
                function() { console.log(elements); return elements[0].childNodes.length; },
                function(newValue, oldValue) { console.log(newValue, oldValue); }
            );
        }
    };
});
