//# sourceURL=boxplotController.js

"use strict";

window.smartR.boxplotController = function(model, ajaxServices, components) {

    var controller = new window.smartR.BaseController(ajaxServices);

    controller.fetch = function(allConcepts) {
        model.clearLoadedData();

        // TODO: validation
        components.fetchDataStep.run(allConcepts)
            .done(function() { components.summaryStats.run('fetch') });
    };

    controller.run = function (paramObj) {
        // TODO: validation?
        components.runStep.run();
    };

    return controller;
};

