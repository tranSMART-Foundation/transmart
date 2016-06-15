//# sourceURL=workflowControls.js

'use strict';

window.smartRApp.directive('workflowControls', [
    '$rootScope',
    function($rootScope) {
        return {
            restrict: 'E',
            transclude: true,
            templateUrl: $rootScope.smartRPath + '/js/smartR/_angular/templates/workflowControls.html',
            link: function(scope, element) {
                scope.$evalAsync(function() {
                    var templateHider = element.children()[0],
                        templateControls = element.children()[1];
                    scope.hidden = false;
                    templateHider.style.bottom = templateControls.clientHeight + 'px';
                    templateHider.addEventListener('click', function() {
                        if (scope.hidden) {
                            templateControls.style.visibility = 'visible';
                            templateHider.style.bottom = templateControls.clientHeight + 'px';
                            $(templateHider).text('Hide');
                            scope.hidden = false;
                        } else {
                            templateControls.style.visibility = 'hidden';
                            templateHider.style.bottom = 0;
                            $(templateHider).text('Show');
                            scope.hidden = true;
                        }
                    });
                });
            }
        };
    }
]);
