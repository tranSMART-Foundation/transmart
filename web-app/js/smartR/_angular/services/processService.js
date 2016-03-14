//# sourceURL=processService.js

window.smartRApp.factory('processService', [ function() {

    var PREPROCESS_BTN = 'preprocessButton',
        FETCH_BTN = 'fetchButton',
        RUN_BTN = 'runButton',
        CAPTURE_BTN = 'captureButton',
        DOWNLOAD_BTN = 'downloadResultsButton',

        service = {
            buttons : {}
        };


    var _emptyResult = function (label, attr) {
        if (service.buttons.hasOwnProperty(label)) {
            service.buttons[label][attr] = {};
        }
    };

    service.toggleButton = function (label, value) {
        if (service.buttons.hasOwnProperty(label)) {
            service.buttons[label].disabled = value;
        }
    };
    
    service.registerButton = function (button, label) {
        service.buttons[label] = button;
    };

    service.onFetching = function (value) {
        service.toggleButton(PREPROCESS_BTN, value);
        service.toggleButton(RUN_BTN, value);
        if (value) {
            _emptyResult(PREPROCESS_BTN, 'summaryData');
            _emptyResult(RUN_BTN, 'storage');
            service.toggleButton(CAPTURE_BTN, true);
            service.toggleButton(DOWNLOAD_BTN, true);
        }
    };

    service.onPreprocessing = function (value) {
        service.toggleButton(FETCH_BTN, value);
        service.toggleButton(RUN_BTN, value);
        if (value) {
            _emptyResult(RUN_BTN, 'storage');
            service.toggleButton(CAPTURE_BTN, true);
            service.toggleButton(DOWNLOAD_BTN, true);
        }
    };

    service.onRunning = function (value) {
        service.toggleButton(FETCH_BTN, value);
        service.toggleButton(PREPROCESS_BTN, value);
        service.toggleButton(CAPTURE_BTN, value);
        service.toggleButton(DOWNLOAD_BTN, value);
    };

    return service;
}]);
