//# sourceURL=boxplotModel.js

"use strict";

window.smartR.boxplotModel = function(components) {
    var model = new window.smartR.Observable();

    var conceptBoxCollection = components.conceptBoxCollection;

    model.getAllConcepts =
        conceptBoxCollection.getAllConcepts.bind(conceptBoxCollection);

    // expose runStep component
    model.getRunOutput = function BoxPlotModel_getRunOutput() {
        // yep, it's JSON encoded as a string inside JSON...
        return JSON.parse(components.runStep.runOutput['value']);
    };
    components.runStep.forwardEvent('runData', model);

    return model;
};
