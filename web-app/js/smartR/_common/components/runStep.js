//# sourceURL=runStep.js

"use strict";

window.smartR.components.runStep = function runStep(ajaxServices,
                                                    executionStatus,
                                                    outputKeys, /* can be [] */
                                                    jsonFileToDownload /* can be empty */) {
    outputKeys = outputKeys || [];

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
                phase: 'run',
            };

            var artifacts;
            var promise = ajaxServices.startScriptExecution(taskData)
                .pipe(function(data) {
                    artifacts = data.result.artifacts;

                    var missingKeys = outputKeys.filter(function(key) {
                        return artifacts[key] === undefined;
                    });

                    if (missingKeys.length > 0) {
                        var def = jQuery.Deferred();
                        def.reject('Error: not found in output: ' + missingKeys);
                        return def.promise();
                    }

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

                    outputKeys.forEach(function(key) {
                        finalData[key] = artifacts[key];
                    });

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
