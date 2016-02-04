
smartRApp.directive('conceptBox', [function() {

    function clearVarSelection(divName) {
        $('#' + divName).children().remove()
    }

    function getConcepts(box) {
        return box.childNodes.map(function(childNode) {
            return childNode.getAttribute('conceptid') })
    }

    return {
        restrict: 'E',
        replace: true,
        scope: {
            conceptGroup: '='
        },
        template: '<div class="queryGroupIncludeSmall"></div>',
        controller: function($scope) {

        },
        link: function(scope, element) {
            // activate drag & drop for template element
            scope.$evalAsync(function() {
                var div = Ext.get(element[0]);
                var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
                dtgI.notifyDrop = dropOntoCategorySelection;
            });

            //modify the model when concepts are added or removed
            scope.$watch(
                function() { return element[0].childNodes.length; },
                function() { scope.conceptGroup = [element[0].childNodes.length]; }
            );
        }
    };
}]);
