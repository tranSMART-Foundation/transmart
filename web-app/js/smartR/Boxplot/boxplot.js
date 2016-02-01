//# sourceURL=boxplot.js

"use strict";

window.smartR.initBoxplotAnalysis = function initBoxplotAnalysis() {
    var c = window.smartR.components;

    var ajaxServices = window.smartR.ajaxServices(window.pageInfo.basePath, 'boxplot');

    var conceptBox1 = c.conceptBox();
    var conceptBox2 = c.conceptBox();
    var subsets1    = c.conceptBox();
    var subsets2    = c.conceptBox();

    var executionStatus = c.executionStatus();
    var fetchDataStep = c.fetchDataStep(ajaxServices, executionStatus.forController);
    var summaryStats = c.summaryStats(ajaxServices, executionStatus.forController, fetchDataStep.forModel, 'fetch');
    var runStep = c.runStep(ajaxServices, executionStatus.forController);


    var conceptBoxCollection = c.conceptBoxCollection({
        box1: conceptBox1.forModel,
        box2: conceptBox2.forModel,
        groups1: subsets1.forModel,
        groups2: subsets2.forModel,
    });
    var modelComponents = {
        conceptBoxCollection: conceptBoxCollection.forModel,
        fetchDataStep: fetchDataStep.forModel,
        summaryStats: summaryStats.forModel,
        runStep: runStep.forModel,
    };
    var model = window.smartR.boxplotModel(modelComponents);


    var controllerComponents = {
        executionStatus: executionStatus.forController,
        fetchDataStep: fetchDataStep.forController,
        summaryStats: summaryStats.forController,
        runStep: runStep.forController,
    };
    var controller = window.smartR.boxplotController(model, ajaxServices, controllerComponents);


    var downloadSvg = c.svgDownload();
    var viewComponents = {
        box1: conceptBox1.forView,
        box2: conceptBox2.forView,
        groups1: subsets1.forView,
        groups2: subsets2.forView,
        summaryStats: summaryStats.forView,
        downloadSvg: downloadSvg.forView,
    };
    var view = new window.smartR.boxplotView(controller, model, viewComponents);

    view.init();
};

window.smartR.initBoxplotAnalysis();
