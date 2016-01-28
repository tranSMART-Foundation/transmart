//# sourceURL=boxplotController.js

"use strict";

window.smartR.boxplotController = function(model, ajaxServices, controllerComponents) {

    var controller = new window.smartR.BaseController(ajaxServices);

    controller.fetch = function(allConcepts, subsets) {

        model.lastFetchedLabels = Object.keys(allConcepts);

        return ajaxServices.startScriptExecution({
            arguments: {
                conceptKeys: allConcepts,
                resultInstanceIds: subsets
            },
            taskType: 'fetchData',
            phase: 'fetch'
        });
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
        controllerComponents.runStep.run();
    };

    return controller;
};

