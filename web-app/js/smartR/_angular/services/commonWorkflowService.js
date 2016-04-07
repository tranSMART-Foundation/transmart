//# sourceURL=commonWorkflowService.js

window.smartRApp.factory('commonWorkflowService', ['rServeService', '$css', function(rServeService, $css) {

    var service = {};

    var fetchFromObject = function (obj, prop){
        //property not found
        if(typeof obj === 'undefined') return false;

        //index of next property split
        var _index = prop.indexOf('.');

        //property split found; recursive call
        if(_index > -1){
            //get object at property (before split), pass on remainder
            return fetchFromObject(obj[prop.substring(0, _index)], prop.substr(_index+1));
        }

        //no split; get property
        return obj[prop];
    };

    var getModels = function (labels) {
        var models = [];
        if (angular.isArray(labels)) {
            labels.forEach (function (label) {
                models.push(fetchFromObject(service.currentScope, label));
            });
        }
        return models;
    };

    service.initializeWorkflow = function(workflowName, scope) {
        service.currentScope = scope;
        // load workflow specific css
        $css.bind({
            href: scope.smartRPath + '/css/' + workflowName + '.css'
        }, scope);

        rServeService.destroyAndStartSession(workflowName);
    };

    return service;

}]);
