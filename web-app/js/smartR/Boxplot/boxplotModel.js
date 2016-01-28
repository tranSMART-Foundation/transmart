//# sourceURL=boxplotModel.js

"use strict";

window.smartR.boxplotModel = function(components, conceptBoxCollectionFactory) {
    var model = {};

    model.concepts1 = components.conceptBox1;
    model.concepts2 = components.conceptBox2;

    model.subsets1 = components.subsets1;
    model.subsets2 = components.subsets2;

    var conceptBoxCollection = conceptBoxCollectionFactory({
        box1: components.conceptBox1,
        box2: components.conceptBox2,
        groups1: components.subsets1,
        groups2: components.subsets2
    });

    model.getAllConcepts = conceptBoxCollection.getAllConcepts;

    return model;
};
