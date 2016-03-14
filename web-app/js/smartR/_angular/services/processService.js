//# sourceURL=processService.js

window.smartRApp.factory('processService', [ function() {

    var PREPROCESS_BTN = 'preprocessButton',
        FETCH_BTN = 'fetchButton',
        RUN_BTN = 'runButton',
        CAPTURE_BTN = 'captureButton',
        DOWNLOAD_BTN = 'downloadResultsButton',
        SORTING_CRITERIA = 'sortingCriteria',

        service = {
            components : {},
            status : {
                SUCCESS : 'success',
                INPROGRESS : 'inprogress',
                ERROR : 'error'
            }
        };

    var _toggleButton = function (label, value) {
        if (service.components.hasOwnProperty(label)) {
            service.components[label].disabled = value;
        }
    };

    var _toggleRankCriteria = function (label, noOfSamples, noOfSubsets) {
        if (!angular.isUndefined(noOfSamples) && !angular.isUndefined(noOfSubsets)) {
            service.components[label].subsets = noOfSubsets;
            if (noOfSubsets > 1) {
                service.components[label].criteria = 'bval';
            } else {
                service.components[label].criteria = 'coef';
            }
            if (noOfSamples === 1) {
                service.components[label].criteria = 'mean';
            }
        }
    };

    var _emptyResult = function (label, attr) {
        if (service.components.hasOwnProperty(label)) {
            service.components[label][attr] = {};
        }
    };

    service.registerComponent = function (component, label) {
        if (!service.hasOwnProperty(label)) {
            service.components[label] = component;
        }
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
            var _sumData = service.components[FETCH_BTN].summaryData;
            // toggle preprocess button based on number of fetched samples
            // disable it when sample only one.
            _toggleButton(PREPROCESS_BTN, _sumData.allSamples <= 1);
            // toggle rank criteria based on number of fetched samples and number of subsets
            _toggleRankCriteria(SORTING_CRITERIA, _sumData.allSamples, _sumData.subsets);
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
