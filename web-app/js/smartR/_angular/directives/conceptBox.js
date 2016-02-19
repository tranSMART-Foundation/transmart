
window.smartRApp.directive('conceptBox', ['$rootScope', function($rootScope) {

    function clearWindow(dom) {
        $(dom).children().remove()
    }

    function getConcepts(dom) {
        return $(dom).children().toArray().map(function(childNode) {
            return childNode.getAttribute('conceptid');
        });
    }

    function activateDragAndDrop(dom) {
        var extObj = Ext.get(dom);
        var dtgI = new Ext.dd.DropTarget(extObj, {ddGroup: 'makeQuery'});
        dtgI.notifyDrop = dropOntoCategorySelection;
    }

    return {
        restrict: 'E',
        scope: {
            conceptGroup: '=',
            label: '=',
            alt: '='
        },
        templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/conceptBox.html',
        link: function(scope, element, attrs) {
            var template_box = element[0].querySelector('.sr-drop-input');
            var template_btn = element[0].querySelector('.sr-drop-btn');

            // activate drag & drop for our conceptBox once it is rendered
            scope.$evalAsync(function() {
                activateDragAndDrop(template_box);
            });

            // bind the button to its clearing functionality
            template_btn.addEventListener('click', function() {
                clearWindow(template_box);
            });

            // this watches the childNodes of the conceptBox and updates the model on change
            new MutationObserver(function() {
                scope.conceptGroup = getConcepts(template_box); // update the model
                scope.$apply();
            }).observe(template_box, { childList: true });
        }
    };
}]);
