//# sourceURL=boxplotView.js

"use strict";

window.smartR.boxplotView = function(controller,
                                     model,
                                     executionStatusFactory,
                                     downloadSvgFactory) {

    var view = {};

    view.container = $('#heim-tabs');

    var executionStatus = executionStatusFactory();

    var downloadSvg = downloadSvgFactory('heim-run-output', 'heim-btn-snapshot-image');
    /* END component instantiation */

    view.fetchBoxplotBtn = $('#sr-btn-fetch-boxplot');
    view.runBoxplotBtn = $('#sr-btn-run-boxplot');
    view.runOutputContainer = $('#heim-run-output');

    view.init = function() {
        // init controller
        controller.init();

        //init tab
        view.container.tabs();

        // bind ui
        bindUIActions();

        // watch model
        watchModel();
    };

    function bindUIActions() {
        view.runBoxplotBtn.on('click', function() {
            controller.run();
        });

        view.fetchBoxplotBtn.on('click', function() {
            var promise = window.smartR.util.getSubsetIds().pipe(function(subsets) {
                return controller.fetch(model.getAllConcepts(), subsets);
            }, function() {
                return 'Could not create subsets.';
            });

            executionStatus.bindPromise(promise, 'Fetching data');
        });
    }

    function watchModel() {
        model.on('runData', function() {
            view.runOutputContainer
                .empty()
                .append($('<div id="controls">'))
                .append($('<div id="boxplot1">'))
                .append($('<div id="boxplot2">'));
            window.SmartRBoxplot.create(model.getRunOutput());
        });
    }

    return view;
};
