//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');
    var conceptFactory = window.smartR.components.conceptBox;
    var conceptBoxCollectionFactory = window.smartR.components.conceptBoxCollection;

    var model = window.smartR.boxplotModel();
    var controller = window.smartR.boxplotController(model, ajaxServices);
    var view = new window.smartR.boxplotView(controller, model,
        conceptFactory, conceptBoxCollectionFactory);
    view.init();
};

window.smartR.initBoxplotAnalysis();
