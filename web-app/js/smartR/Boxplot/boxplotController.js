//# sourceURL=boxplotController.js

"use strict";

window.smartR.boxplotController = function(model, ajaxServices) {

    var controller = new window.smartR.BaseController(ajaxServices);

    controller.model = model;
    controller.service = ajaxServices;

    controller.fetch = function(allConcepts, subsets) {
        return ajaxServices.startScriptExecution({
            arguments: {
                conceptKeys: allConcepts,
                resultInstanceIds: subsets
            },
            taskType: 'fetchData',
            phase: 'fetch'
        });
    };

    controller.run = function (paramObj) {

    };

    return controller;
};

