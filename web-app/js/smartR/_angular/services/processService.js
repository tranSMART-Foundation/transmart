//# sourceURL=processService.js

window.smartRApp.factory('processService', [ function() {

    var service = {
        buttons : {}
    };

    service.registerButton = function (button, label) {
        service.buttons[label] = button;
    };

    service.onFetching = function (isOnFetching) {
        if (isOnFetching) {
            service.buttons.preprocessButton.disabled = true;
            service.buttons.preprocessButton.summaryData = {};
            service.buttons.runButton.disabled = true;
            service.buttons.runButton.storage = {};
            service.buttons.captureButton.disabled = true;
            service.buttons.downloadResultsButton.disabled = true;
        } else {
            service.buttons.preprocessButton.disabled = false;
            service.buttons.runButton.disabled = false;
        }
    };

    service.onPreprocessing = function (isOnPreprocessing) {
        if (isOnPreprocessing) {
            service.buttons.fetchButton.disabled = true;
            service.buttons.runButton.disabled = true;
            service.buttons.runButton.storage = {};
            service.buttons.captureButton.disabled = true;
            service.buttons.downloadResultsButton.disabled = true;
        } else {
            service.buttons.fetchButton.disabled = false;
            service.buttons.runButton.disabled = false;
        }
    };

    service.onRunning = function (isOnRunning) {
        if (isOnRunning) {
            service.buttons.fetchButton.disabled = true;
            service.buttons.preprocessButton.disabled = true;
            service.buttons.captureButton.disabled = true;
            service.buttons.downloadResultsButton.disabled = true;
        } else {
            service.buttons.fetchButton.disabled = false;
            service.buttons.preprocessButton.disabled = false;
            service.buttons.captureButton.disabled = false;
            service.buttons.downloadResultsButton.disabled = false;
        }
    };

    return service;
}]);
