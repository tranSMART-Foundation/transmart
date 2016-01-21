//# sourceURL=boxplotView.js

"use strict";

window.smartR.BoxplotView = function (controller, model) {

    var view = this;

    view.controller = controller;
    view.model = model;

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

    };

    view.init();
};

