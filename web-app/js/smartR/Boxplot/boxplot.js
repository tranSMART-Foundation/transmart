//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {
    var c = window.smartR.components;

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');
    var executionStatusFactory = c.executionStatus;

    var conceptBox1 = c.conceptBox('concept1', 'sr-concept1-btn');
    var conceptBox2 = c.conceptBox('concept2', 'sr-concept2-btn');
    var subsets1    = c.conceptBox('subsets1', 'sr-subset1-btn');
    var subsets2    = c.conceptBox('subsets2', 'sr-subset2-btn');

    var conceptBoxCollection = c.conceptBoxCollection({
        box1: conceptBox1,
        box2: conceptBox2,
        groups1: subsets1,
        groups2: subsets2,
    });
    var executionStatus = c.executionStatus();
    var runStep = c.runStep(ajaxServices, executionStatus);

    var modelComponents = {
        conceptBoxCollection: conceptBoxCollection,
        runStep: runStep.forModel,
    };
    var model = window.smartR.boxplotModel(modelComponents);

    var controllerComponents = {
        runStep: runStep.forController,
    };
    var controller = window.smartR.boxplotController(model, ajaxServices, controllerComponents);

    var downloadSvgFactory = c.svgDownload;
    var view = new window.smartR.boxplotView(controller, model, executionStatusFactory, downloadSvgFactory);

    view.init();
};

window.smartR.initBoxplotAnalysis();
