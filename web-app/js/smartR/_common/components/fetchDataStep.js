//# sourceURL=fetchDataStep.js

"use strict";

window.smartR.components.fetchDataStep = function runStep(ajaxServices, executionStatus) {

    var model = new window.smartR.Observable();
    var loadedVariables = [];
    Object.defineProperty(model, 'loadedVariables', {
        get: function() { return loadedVariables; },
        set: function(newLoadedVariables) {
            loadedVariables = newLoadedVariables;
            this.trigger('loadedVariables');
        }
    });

    function FetchDataStep_fetch(allConcepts) {
        var promise = window.smartR.util.getSubsetIds().pipe(function(subsets) {
            return ajaxServices.startScriptExecution({
                arguments: {
                    conceptKeys: allConcepts,
                    resultInstanceIds: subsets
                },
                taskType: 'fetchData',
                phase: 'fetch'
            });
        }, function() {
            return 'Could not create subsets.';
        });

        executionStatus.bindPromise(promise, 'Fetching data');

        promise.done(function(data) {
            model.loadedVariables = data.result.artifacts.currentLabels;
        });

        return promise;
    }

    return {
        forModel: model,
        forController: {
            run: FetchDataStep_fetch,
        },
    };
};
