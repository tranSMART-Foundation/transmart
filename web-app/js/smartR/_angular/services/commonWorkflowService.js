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
     * @param  targetLabels   - array of target labels
     * @param callback (newObject, oldObject, targetArray) - invoked when source obj has new value
     *
     */
    service.registerCondition = function (sourceLabel, targetLabels, callback) {

        var targetObjects = [];

        if (angular.isArray(targetLabels)) {
           targetLabels.forEach (function (targetLabel) {
               targetObjects.push(fetchFromObject(service.currentScope, targetLabel));
           });
        }

        service.currentScope.$watch (sourceLabel, function (n, o) {
            callback(n,  o, targetObjects);
        }, true);
    };

    /**
     * Disable target objects when model changes
     * @param newArray
     * @param oldArray
     * @param targetObjArr
     */
    service.disableComponentsBasedOnInput = function (newArray, oldArray, affectedComponents) {
        affectedComponents.forEach(function (component) {
            if (component.hasOwnProperty('disabled')) {
                component.disabled = newArray.length <= 0;
            }
        });
    };

    /**
     * Disable target objects when summary data changes
     * @param newSourceVal
     * @param oldSourceVal
     * @param targetObjArr
     */
    service.disableComponentsBasedOnResult = function (newSummaryData, oldSummaryData, affectedComponents) {
        console.log(newSummaryData);
        affectedComponents.forEach(function (component) {
            component.disabled = Object.keys(newSummaryData).length == 0;
        });
    };

    service.clearOldResultsOnReFetch = function (newSummaryData, oldSummaryData, affectedComponents) {
        affectedComponents.forEach(function (component) {
            component.scriptResults = {};
        });
    };

    service.disableComponentsBasedOnComponent = function (newComponentState, oldComponentState, affectedComponents) {
        console.log(newComponentState);
        affectedComponents.forEach(function (component) {
            component.disabled = newComponentState.disabled;
        });
    };

    return service;

}]);
