//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');
    var executionStatusFactory = window.smartR.components.executionStatus;
    var summaryTableFactory = window.smartR.components.summaryTable;

    var conceptBox1 = window.smartR.components.conceptBox('concept1', 'sr-concept1-btn');
    var conceptBox2 = window.smartR.components.conceptBox('concept2', 'sr-concept2-btn');
    var subsets1    = window.smartR.components.conceptBox('subsets1', 'sr-subset1-btn');
    var subsets2    = window.smartR.components.conceptBox('subsets2', 'sr-subset2-btn');

    var conceptBoxCollectionFactory = window.smartR.components.conceptBoxCollection;

    var model = window.smartR.boxplotModel({
        conceptBox1: conceptBox1,
        conceptBox2: conceptBox2,
        subsets1: subsets1,
        subsets2: subsets2
    }, conceptBoxCollectionFactory);

    var controller = window.smartR.boxplotController(model, ajaxServices);
    var view = new window.smartR.boxplotView(controller, model, executionStatusFactory, summaryTableFactory);

    view.init();
};

window.smartR.initBoxplotAnalysis();
