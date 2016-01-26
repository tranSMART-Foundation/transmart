//# sourceURL=boxplotView.js

"use strict";

window.smartR.boxplotView = function(controller, model, concept) {

    var view = {};

    view.container = $('#heim-tabs');
    view.conceptBox1 = concept('concept1', 'sr-concept1-btn');
    view.conceptBox2 = concept('concept2', 'sr-concept2-btn');
    view.subsets1    = concept('subsets1', 'sr-subset1-btn');
    view.subsets2    = concept('subsets2', 'sr-subset2-btn');
    view.fetchBoxplotBtn = $('#sr-btn-fetch-boxplot');
    view.runBoxplotBtn = $('#sr-btn-run-boxplot');

    view.init = function() {
        // init controller
        controller.init();

        // extend model properties
        model['concepts1'] = view.conceptBox1.model;
        model['concepts2'] = view.conceptBox2.model;
        model['subsets1'] = view.subsets1.model;
        model['subsets2'] = view.subsets2.model;

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
            });

            promise.done(function(data) {
                window.alert(JSON.stringify(data));
            });
        });

    }

    return view;
};
