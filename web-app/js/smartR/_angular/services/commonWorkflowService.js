
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

    service.initializeWorkflow = function(workflowName, scope) {
        service.currentScope = scope;
        // load workflow specific css
        $css.bind({
            href: scope.smartRPath + '/css/' + workflowName + '.css'
        }, scope);

        rServeService.destroyAndStartSession(workflowName);
    };

    /**
     * Register sourceLabel as object reference. If there's any changes on this object, will invoke callback
     * @param  sourceLabel      - reference object label
     * @param  targetLabelArr   - array of target labels
     * @param callback (newObject, oldObject, targetArray) - invoked when source obj has new value
     *
     */
    service.registerCondition = function (sourceLabel, targetLabelArr, callback) {

        var targetObjects = [];

        if (angular.isArray(targetLabelArr)) {
           targetLabelArr.forEach (function (targetLabel) {
               targetObjects.push(fetchFromObject(service.currentScope, targetLabel));
           });
        }

        service.currentScope.$watch (sourceLabel, function (n, o) {
            callback(n,  o, targetObjects);
        }, true);
    };

    service.disableComponentsBasedOnInput = function (newSourceVal, oldSourceVal, targetObjArr) {
        targetObjArr.forEach(function (component) {
            if (component.hasOwnProperty('disabled')) {
                component.disabled = newSourceVal.length <= 0;
            }
        });
    };

    service.disableComponentsBasedOnResults = function (newSourceVal, oldSourceVal, targetObjArr) {
        targetObjArr.forEach(function (component) {
            component.disabled = angular.isUndefined(newSourceVal.allSamples);
        });
    };

    return service;

}]);
