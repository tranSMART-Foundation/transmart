//# sourceURL=processService.js

window.smartRApp.factory('processService', [ function() {

    var PREPROCESS_BTN = 'preprocessButton',
        FETCH_BTN = 'fetchButton',
        RUN_BTN = 'runButton',
        CAPTURE_BTN = 'captureButton',
        DOWNLOAD_BTN = 'downloadResultsButton',

        service = {
            buttons : {},
            status : {
                SUCCESS : 'success',
                INPROGRESS : 'inprogress',
                ERROR : 'error'
            }
        };

    var _toggleButton = function (label, value) {
        if (service.buttons.hasOwnProperty(label)) {
            service.buttons[label].disabled = value;
        }
    };

    // TODO
    var _toggleRankCriteria = function (label, noOfSubsets) {
        if (noOfSubsets > 1) {

        }
    };

    var _emptyResult = function (label, attr) {
        if (service.buttons.hasOwnProperty(label)) {
            service.buttons[label][attr] = {};
        }
    };

    service.registerButton = function (button, label) {
        service.buttons[label] = button;
    };

    service.onFetching = function (value, status) {
        _toggleButton(RUN_BTN, value);
        if (value) {
            _emptyResult(PREPROCESS_BTN, 'summaryData');
            _emptyResult(RUN_BTN, 'storage');
            _toggleButton(CAPTURE_BTN, true);
            _toggleButton(DOWNLOAD_BTN, true);
        }
        if (!value && status === service.status.SUCCESS) {
            // toggle preprocess button based on number of fetched samples
            // disable it when sample only one.
            _toggleButton(PREPROCESS_BTN, service.buttons[FETCH_BTN].summaryData.allSamples <= 1);
        } else {
            _toggleButton(PREPROCESS_BTN, value);
        }
    };

    service.onPreprocessing = function (value) {
        _toggleButton(FETCH_BTN, value);
        _toggleButton(RUN_BTN, value);
        if (value) {
            _emptyResult(RUN_BTN, 'storage');
            _toggleButton(CAPTURE_BTN, true);
            _toggleButton(DOWNLOAD_BTN, true);
        }
    };

    service.onRunning = function (value) {
        _toggleButton(FETCH_BTN, value);
        _toggleButton(PREPROCESS_BTN, value);
        _toggleButton(CAPTURE_BTN, value);
        _toggleButton(DOWNLOAD_BTN, value);
    };

    return service;
}]);
