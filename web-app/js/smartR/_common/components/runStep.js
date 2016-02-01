//# sourceURL=runStep.js

"use strict";

window.smartR.components.runStep = function runStep(ajaxServices,
                                                    executionStatus,
                                                    jsonFileToDownload /* can be empty */) {

    var model = new window.smartR.Observable();
    var runOutput = {};
    Object.defineProperty(model, 'runOutput', {
        get: function() { return runOutput; },
        set: function(value) {
            runOutput = value;
            this.trigger('runData');
        }
    });

    var controller = {
        run: function RunStepController_run(args) {
            args = args || {};
            var taskData = {
                arguments: args,
                taskType: 'run',
            };

            var artifacts;
            var promise = ajaxServices.startScriptExecution(taskData)
                .pipe(function(data) {
                    artifacts = data.result.artifacts;

                    if (jsonFileToDownload) {
                        if (artifacts.files.indexOf(jsonFileToDownload) == -1) {
                            var def = jQuery.Deferred();
                            def.reject('Error: file ' + jsonFileToDownload + ' was not generated');
                            return def.promise();
                        }

                        return ajaxServices.downloadFile(
                            data.executionId, jsonFileToDownload);
                    } else {
                        return undefined;
                    }
                }).pipe(function(fileData) {
                    var finalData = {};

                    if (fileData !== undefined) {
                        finalData.file = fileData;
                    }

                    finalData['value'] = artifacts['value'];

                    model.runOutput = finalData;
                });

            executionStatus.bindPromise(promise, 'Running analysis');
        },
    };

    return {
        forController: controller,
        forModel: model,
    };
};
