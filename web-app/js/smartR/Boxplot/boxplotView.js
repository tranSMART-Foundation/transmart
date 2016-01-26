//# sourceURL=boxplotView.js

"use strict";

window.smartR.boxplotView = function(controller, model) {

    var view = {};

    view.container = $('#heim-tabs');
    view.fetchBoxplotBtn = $('#sr-btn-fetch-boxplot');
    view.runBoxplotBtn = $('#sr-btn-run-boxplot');

    view.init = function() {
        controller.init();

        view.container.tabs();

        bindUIActions();
    };

    function bindUIActions() {
        view.runBoxplotBtn.on('click', function() {
            view.controller.run();
        });

        view.fetchBoxplotBtn.on('click', function() {
            var promise = window.smartR.util.getSubsetIds().pipe(function(subsets) {
                return controller.fetch(model.getAllConcepts(), subsets);
            });

            promise.done(function(data) {
                window.alert(JSON.stringify(data));
            });
        });

    };

    return view;
};
