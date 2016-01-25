//# sourceURL=boxplotController.js

"use strict";

window.smartR.BoxplotController = function(model, ajaxServices) {

    var controller = this, helper = HeimExtJSHelper;
    controller.model = model;
    controller.service = ajaxServices;

    controller.fetch = function (paramObj) {
        var defer = $.Deferred();

        // fetch data
        ajaxServices.startScriptExecution({
            arguments: helper.createAnalysisConstraints(paramObj),
            taskType: 'fetchData'
        }).done(function (d) {
            // TODO return result to view layer
            defer.resolve(d);
        }).fail(function (jq, status, message) {
            defer.reject(message);
        });

        return defer.promise();
    };

    controller.run = function (paramObj) {
        // TODO invoke run and retrieve json data
        SmartRBoxplot.create(controller.model.json);
    };

    return controller;
};

