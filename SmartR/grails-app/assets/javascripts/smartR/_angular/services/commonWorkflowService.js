//# sourceURL=commonWorkflowService.js

'use strict';
// console.log("initialize Service commonWorkflowService.js");

window.smartRApp.factory('commonWorkflowService', ['rServeService', '$css', function(rServeService, $css) {

    var service = {};

    service.initializeWorkflow = function(workflowName, scope) {
        service.currentScope = scope;
        // load workflow specific css
        $css.bind({
            href: scope.smartRPath + '/assets/' + workflowName + '.css'
        }, scope);

        rServeService.destroyAndStartSession(workflowName);
    };

    return service;

}]);
