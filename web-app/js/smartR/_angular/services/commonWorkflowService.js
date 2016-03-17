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

    /**
     * Register sourceLabel as object reference. If there's any changes on this object, will invoke callback
     * @param  sourceLabels - reference object label
     * @param  targetLabels - array of target labels
     * @param callback (newObject, oldObject, targetArray) - invoked when source obj has new value
     *
     */
    service.registerCondition = function (sourceLabels, targetLabels, callback) {
        var _scope = service.currentScope,
            targetModels = getModels(targetLabels);

        _scope.$watchGroup (sourceLabels, function (n, o) {
            callback(n,  o, _scope, targetModels);
        });
    };

    /**
     *
     * @param newValues
     * @param oldValues
     * @param scope
     * @param affectedComponents
     */
    service.whenSelectHighDimensionalNodes = function (newValues, oldValues, scope, affectedComponents) {
        affectedComponents.forEach(function (component) {
            if (component.hasOwnProperty('disabled')) {
                component.disabled = newValues[0].length <= 0;
            }
        });
    };

    service.whenFetchData =  function (newValues, oldValues, scope, affectedComponents) {
        var preprocessModel = affectedComponents[0],
            runModel = affectedComponents[1],
            fetchBtnFlag = newValues[0],
            fetchSamples = newValues[1],
            fetchSubsets = newValues[2]; // TODO show/hide rank criteria based on this

        console.log(newValues);

        if (fetchBtnFlag) { // empty preprocess & run result when fetching
            preprocessModel.scriptResults = {};
            runModel.scriptResults = {};
        }

        preprocessModel.btn.disabled = fetchBtnFlag.disabled;
        preprocessModel.btn.disabled = fetchSamples <= 1; // enable button only when samples > 1
        runModel.btn.disabled = fetchBtnFlag.disabled;
    };

    return service;

}]);
