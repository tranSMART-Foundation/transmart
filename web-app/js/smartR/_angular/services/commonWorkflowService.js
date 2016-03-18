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
     * @param callback (newObject, oldObject, scope, targetArray) - invoked when source obj has new value
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
     * Components are enabled/disabled according to presence of high dimensional nodes
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

    /**
     * Collective behaviour when fetching data for heatmap
     * @param newValues - new values of components models that determine other components states
     * @param oldValues - old values of components models that determine other components states
     * @param scope
     * @param affectedComponents - list of affected component's models
     */
    service.whenFetchHeatmapData =  function (newValues, oldValues, scope, affectedComponents) {

        var preprocessModel = affectedComponents[0],
            runModel = affectedComponents[1],
            fetchBtnFlag = newValues[0],
            fetchSamples = newValues[1],
            fetchSubsets = newValues[2];
        
        
        var _toggleRankCriteria = function (runAnalysisModel, noOfSamples, noOfSubsets) {

            runAnalysisModel.subsets = noOfSubsets;

            if (noOfSubsets > 1) {
                runAnalysisModel.params.ranking = 'bval';
            } else {
                runAnalysisModel.params.ranking = 'coef';
            }
            if (noOfSamples === 1) {
                runAnalysisModel.params.ranking = 'mean';
            }
        };


        if (fetchBtnFlag) { // empty preprocess & run result when fetching
            preprocessModel.scriptResults = {};
            runModel.scriptResults = {};
        } else {
            if (fetchSamples > 0 && fetchSubsets > 0) {
                _toggleRankCriteria(runModel, fetchSamples, fetchSubsets);
            }
        }

        preprocessModel.btn.disabled = fetchBtnFlag;
        preprocessModel.btn.disabled = runModel.btn.disabled ? true : fetchSamples <= 1;
        runModel.btn.disabled = fetchBtnFlag;
    };

    /**
     * Collective behaviour when preprocess data for heatmap
     * @param newValues
     * @param oldValues
     * @param scope
     * @param affectedComponents
     */
    service.whenPreprocessHeatmapData = function (newValues, oldValues, scope, affectedComponents) {
        var preprocessBtnFlag = newValues[0],
            fetchModel = affectedComponents[0],
            runModel = affectedComponents[1];

        // toggle fetch & run buttons
        fetchModel.btn.disabled = preprocessBtnFlag;
        runModel.btn.disabled = preprocessBtnFlag;

        if (preprocessBtnFlag) {
            runModel.scriptResults = {};
        }
    };

    /**
     * Collective behaviour when running heatmap
     * @param newValues
     * @param oldValues
     * @param scope
     * @param affectedComponents
     */
    service.whenRunHeatmapAnalysis = function (newValues, oldValues, scope, affectedComponents) {
        var runBtnFlag = newValues[0],
            runResults = newValues[1],
            fetchModel = affectedComponents[0],
            preprocessModel = affectedComponents[1],
            downloadBtns = affectedComponents[2];

        // toggle downloads related buttons
        downloadBtns.disabled = Object.keys(runResults).length !== 0;

        // toggle fetch & preprocess buttons
        fetchModel.btn.disabled = runBtnFlag;
        preprocessModel.btn.disabled = runBtnFlag;
    };

    return service;

}]);
