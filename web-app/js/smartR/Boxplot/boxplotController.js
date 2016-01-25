//# sourceURL=boxplotController.js

"use strict";

window.smartR.BoxplotController = function(model, ajaxServices) {

    var controller = this, helper = HeimExtJSHelper;
    controller.model = model;
    controller.service = ajaxServices;

    controller.fetch = function (paramObj) {
        var defer = $.Deferred();

        //
        ajaxServices.startScriptExecution({
            arguments: helper.createAnalysisConstraints(paramObj),
            taskType: 'fetchData'
        }).done(function (d) {

            defer.resolve(d);
        }).fail(function (jq, status, message) {
            defer.reject([jq, status, message]);
        });

        return defer.promise();
    };

    controller.generateBoxplot = function () {
        ajaxServices.startScriptExecution({
            arguments:{},
            taskType: 'boxplot'
        });
    };

    return controller;
};

