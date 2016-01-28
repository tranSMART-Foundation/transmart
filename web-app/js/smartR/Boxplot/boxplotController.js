//# sourceURL=boxplotController.js

"use strict";

window.smartR.boxplotController = function(model, ajaxServices, components) {

    var controller = new window.smartR.BaseController(ajaxServices);

    controller.fetch = function(allConcepts) {
        // TODO: validation
        components.fetchDataStep.run(allConcepts);
        // TODO: call summary at the end:
        // .done(function() { this.summary(); }.bind(this));
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

