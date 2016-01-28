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

    view.init = function() {
        // init controller
        controller.init();

        //init tab
        view.container.tabs();

        // bind ui
        bindUIActions();
    };

    function bindUIActions() {
        view.runBoxplotBtn.on('click', function() {
            view.controller.run();
        });

        view.fetchBoxplotBtn.on('click', function() {
            var promise = window.smartR.util.getSubsetIds().pipe(function(subsets) {
                return controller.fetch(model.getAllConcepts(), subsets);
            }, function() {
                return 'Could not create subsets.';
            });

            executionStatus.bindPromise(promise, 'Fetching data');

            promise.done(function(data) {
                window.alert(JSON.stringify(data));
            });
        });
    }

    return view;
};
