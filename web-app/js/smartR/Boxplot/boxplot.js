//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {
    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');

    var conceptBox1 = smartR.components.conceptBox('concept1', 'sr-concept1-btn');
    var conceptBox2 = window.smartR.components.conceptBox('concept2', 'sr-concept2-btn');
    var subsets1    = window.smartR.components.conceptBox('subsets1', 'sr-subset1-btn');
    var subsets2    = window.smartR.components.conceptBox('subsets2', 'sr-subset2-btn');

    var model = window.smartR.boxplotModel({
        conceptBox1: conceptBox1.model,
        conceptBox2: conceptBox2.model,
        subsets1: subsets1.model,
        subsets2: subsets2.model,
    });

    var controller = window.smartR.boxplotController(model, ajaxServices);
    var view = new window.smartR.boxplotView(controller, model);

    view.init();
};

window.smartR.initBoxplotAnalysis();
