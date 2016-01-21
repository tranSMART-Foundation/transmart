//# sourceURL=boxplotController.js

"use strict";

window.smartR.BoxplotController = function(model, ajaxServices) {

    var controller = this, helper = HeimExtJSHelper;
    controller.model = model;
    controller.service = ajaxServices;

    controller.fetch = function (paramObj) {
        return ajaxServices.startScriptExecution({
            arguments: helper.createAnalysisConstraints(paramObj),
            taskType: 'fetchData'
        });
    };

    controller.generateBoxplot = function () {
        ajaxServices.startScriptExecution({
            arguments:{},
            taskType: 'boxplot'
        });
    };

    return controller;
};

