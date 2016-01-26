//# sourceURL=boxplotModel.js

"use strict";

window.smartR.boxplotModel = function(components) {
    var model = {};

    function prefixObjectKeys(targetObject, prefix, object) {
        Object.keys(object).forEach(function(key) {
            var newKey = prefix + key;
            targetObject[newKey] = object[key];
        });
    }

    model.getAllConcepts = function BoxPlotModel_getAllConcepts() {
        var res = {};

        prefixObjectKeys(res, 'box1_', this.concepts1.getLabelledConcepts());
        prefixObjectKeys(res, 'box2_', this.concepts2.getLabelledConcepts());
        prefixObjectKeys(res, 'groups1_', this.subsets1.getLabelledConcepts());
        prefixObjectKeys(res, 'groups2_', this.subsets2.getLabelledConcepts());
        return res;
    };

    return model;
};
