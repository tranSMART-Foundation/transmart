//# sourceURL=boxplotModel.js

"use strict";

window.smartR.boxplotModel = function(components) {
    var model = {};

    model.concepts1 = components.conceptBox1;
    model.concepts2 = components.conceptBox2;

    model.subsets1 = components.subsets1;
    model.subsets2 = components.subsets2;

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
