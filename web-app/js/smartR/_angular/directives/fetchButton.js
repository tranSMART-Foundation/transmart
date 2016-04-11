//# sourceURL=fetchButton.js

'use strict';

window.smartRApp.directive('fetchButton', [
    '$rootScope',
    'rServeService',
    'smartRUtils',
    function($rootScope, rServeService, smartRUtils) {
        return {
            restrict: 'E',
            scope: {
                loaded: '=',
                running: '=',
                conceptMap: '=',
                biomarkers: '=?',
                showSummaryStats: '=?',
                summaryData: '=?',
                allSamples: '=?',
                subsets: '=?'
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/fetchButton.html',
            link: function(scope, element) {

                var template_btn = element.children()[0],
                    template_msg = element.children()[1];

                var _init = function () {
                    template_btn.disabled = true;
                    scope.summaryData = {}; // reset
                    scope.allSamples = 0;
                    scope.subsets = 0;
                    scope.running = true;
                    template_msg.innerHTML = 'Fetching data, please wait <span class="blink_me">_</span>';
                };

                var _onSuccess = function() {
                    template_msg.innerHTML = 'Task completed!';
                    scope.subsets = smartRUtils.countCohorts();
                    scope.loaded = true;
                    template_btn.disabled = false;
                    scope.running = false;
                };

                var _onFailure = function(msg) {
                    template_msg.innerHTML = 'Failure: ' + msg;
                    scope.loaded = false;
                    template_btn.disabled = false;
                    scope.running = false;
                };

                // we add this conditional $watch because there is some crazy promise resolving for allSamples
                // going on. This is a workaround which observes allSamples and uses it as criteria for successful
                // completion.
                if (scope.showSummaryStats) {
                    scope.$watch('summaryData', function(newValue) {
                        // prevents initial firing
                        if (scope.running && Object.keys(newValue).indexOf('subsets') !== -1) {
                            scope.allSamples = scope.summaryData.allSamples;
                            _onSuccess();
                        }
                    }, true);
                }

                var _getDataConstraints = function (biomarkers) {
                    if (typeof biomarkers !== 'undefined' && biomarkers.length > 0) {
                        var searchKeywordIds = biomarkers.map(function(biomarker) {
                            return String(biomarker.id);
                        });
                        return {
                            search_keyword_ids: {
                                keyword_ids: searchKeywordIds
                            }
                        };
                    }
                };

                var _afterDataFetched = function() {
                    if (!scope.showSummaryStats) {
                        _onSuccess();
                        return;
                    }

                    template_msg.innerHTML =
                        'Execute summary statistics, please wait <span class="blink_me">_</span>';

                    return rServeService.executeSummaryStats('fetch')
                        .then(function(data) {
                            scope.summaryData = data.result;
                        }, _onFailure);
                };

                template_btn.onclick = function() {
                    _init();
                    var conceptKeys = smartRUtils.conceptBoxMapToConceptKeys(scope.conceptMap);
                    var dataConstraints = _getDataConstraints(scope.biomarkers);
                    rServeService.loadDataIntoSession(conceptKeys, dataConstraints)
                        .then(_afterDataFetched, _onFailure);
                };
            }
        };
    }]);
