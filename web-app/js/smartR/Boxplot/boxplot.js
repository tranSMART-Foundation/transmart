//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');
    var conceptFactory = new window.smartR.components.ConceptBoxFactory();

    var model = window.smartR.boxplotModel();
    var controller = window.smartR.boxplotController(model, ajaxServices);
    var view = new window.smartR.boxplotView(controller, model, conceptFactory);
    view.init();
};

window.smartR.initBoxplotAnalysis();
