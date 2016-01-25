//# sourceURL=boxplotView.js

"use strict";

window.smartR.BoxplotView = function (controller, model) {

    var view = this, helper = HeimExtJSHelper;

    view.controller = controller;
    view.model = model;
    view.workflow = 'boxplot';

    view.concept1 = {
        inputEl : $('#concept1'),
        clearBtn : $('#sr-concept1-btn')
    };

    view.subset1 = {
        inputEl : $('#subset1'),
        clearBtn : $('#sr-subset1-btn')
    };

    view.concept2 = {
        inputEl : $('#concept2'),
        clearBtn : $('#sr-concept2-btn')
    };

    view.subset2 = {
        inputEl : $('#subset2'),
        clearBtn : $('#sr-subset2-btn')
    };

    view.fetchBoxplotBtn = $('#sr-btn-fetch-boxplot');
    view.runBoxplotBtn = $('#sr-btn-run-boxplot');

    view.init = function () {
        bindUIActions();
    };

    var bindUIActions = function () {

        var _clearInputEl = function (event) {
            event.data.empty();
        };

        view.concept1.clearBtn.on('click', view.concept1.inputEl, _clearInputEl);
        view.subset1.clearBtn.on('click', view.subset1.inputEl, _clearInputEl);
        view.concept2.clearBtn.on('click', view.concept2.inputEl, _clearInputEl);
        view.subset2.clearBtn.on('click', view.subset2.inputEl, _clearInputEl);

        view.runBoxplotBtn.on('click', function () {
            view.controller.run();
        });

        view.fetchBoxplotBtn.on('click', function () {
            var strConcept1 = helper.readConceptVariables(view.concept1.inputEl.attr('id'));
            // TODO get other concepts
            view.controller.fetch({
                conceptPaths: strConcept1,
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
