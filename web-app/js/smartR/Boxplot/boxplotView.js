//# sourceURL=boxplotView.js

"use strict";

window.smartR.BoxplotView = function (controller, model) {

    var view = this, helper = HeimExtJSHelper;

    view.controller = controller;
    view.model = model;
    view.workflow = 'boxplot';

    view.container = $('#heim-tabs');
    view.concepts = [
        { label: 'concept1', inputEl : $('#concept1'), clearBtn : $('#sr-concept1-btn')},
        { label: 'subsets1', inputEl : $('#subsets1'), clearBtn : $('#sr-subset1-btn')},
        { label: 'concept2', inputEl : $('#concept2'), clearBtn : $('#sr-concept2-btn')},
        { label: 'subsets2', inputEl : $('#subsets2'), clearBtn : $('#sr-subset2-btn')}
    ];
    view.fetchBoxplotBtn = $('#sr-btn-fetch-boxplot');
    view.runBoxplotBtn = $('#sr-btn-run-boxplot');

    view.init = function () {
        // init tabs
        view.container.tabs();
        bindUIActions();
    };

    var bindUIActions = function () {

        var _clearInputEl = function (event) {
            event.data.empty();
        };

        view.concepts.forEach(function (concept) {
            concept.clearBtn.on('click', concept.inputEl, _clearInputEl);
        });

        view.runBoxplotBtn.on('click', function () {
            view.controller.run();
        });

        view.fetchBoxplotBtn.on('click', function () {

            var allConcepts = helper.concatenateConceptPath(view.concepts);

            // TODO get other concepts
            view.controller.fetch({
                conceptPaths: allConcepts,
                // CurrentSubsetIDs can contain undefined and null. Pass only nulls forward
                resultInstanceIds : GLOBAL.CurrentSubsetIDs.map(function (v) { return v || null; }),
                searchKeywordIds: ''
            }).done(function (d) {
                console.log(d);
            }).fail(function (jq, status, msg) {
                console.error(msg);
            });
        });

    };

    view.init();
};
