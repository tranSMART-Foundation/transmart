//# sourceURL=boxplotModel.js

"use strict";

window.smartR.boxplotModel = function(components) {
    var model = new window.smartR.Observable();

    var conceptBoxCollection = components.conceptBoxCollection;

    model.getAllConcepts =
        conceptBoxCollection.getAllConcepts.bind(conceptBoxCollection);

    // expose loaded variables
    Object.defineProperty(model, 'loadedVariables', {
        get: function() { return components.fetchDataStep.loadedVariables; },
    });
    components.fetchDataStep.forwardEvent('loadedVariables', model);

    // expose runStep component
    model.getRunOutput = function BoxPlotModel_getRunOutput() {
        // yep, it's JSON encoded as a string inside JSON...
        return JSON.parse(components.runStep.runOutput['value']);
    };
    components.runStep.forwardEvent('runData', model);

    model.clearLoadedData = function BoxplotModel_clearLoadedData() {
        components.fetchDataStep.loadedVariables = [];
        components.summaryStats.clear();
    };

    return model;
};
