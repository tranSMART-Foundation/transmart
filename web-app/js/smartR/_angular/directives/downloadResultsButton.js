//# sourceURL=downloadResultsButton.js

window.smartRApp.directive('downloadResultsButton', ['rServeService', 'smartRUtils', function(rServeService, smartRUtils) {
    return {
        restrict: 'E',
        scope: {

        },
        template: '<input type="button" value="Capture" class="heim-action-button"><span style="padding-left: 10px;"></span>',
        link: function(scope, element) {

        }
    };
}]);
