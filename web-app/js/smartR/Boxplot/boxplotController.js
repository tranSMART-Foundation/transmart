//# sourceURL=boxplotController.js

"use strict";

window.smartR.boxplotController = function(model, ajaxServices, components) {

    var controller = new window.smartR.BaseController(ajaxServices);

    controller.fetch = function(allConcepts) {
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

        components.executionStatus.bindPromise(promise, 'Fetching data');
    };

    controller.summary = function (phase) {
        var _summaryObj = {};
         ajaxServices.startScriptExecution({
            arguments: {
                phase: phase,
                projection: 'log_intensity'
            },
            taskType: 'summary',
            phase: phase
        }).done(function (d) {
             _summaryObj = d;
         });
        return _summaryObj;
    };

    controller.run = function (paramObj) {
        // TODO: validation?
        components.runStep.run();
    };

    return controller;
};

