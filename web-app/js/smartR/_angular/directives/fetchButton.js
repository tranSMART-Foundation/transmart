//# sourceURL=fetchButton.js

'use strict';

window.smartRApp.directive('fetchButton',
    ['$rootScope', 'rServeService', 'smartRUtils', 'processService',
        function($rootScope, rServeService, smartRUtils, processService) {
        return {
            restrict: 'E',
            scope: {
                disabled: '=',
                conceptMap: '=',
                biomarkers: '=',
                showSummaryStats: '=',
                summaryData: '='
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/fetchButton.html',
            link: function(scope, element) {

                var template_btn = element.children()[0],
                    template_msg = element.children()[1];

                template_btn.disabled = scope.disabled;

                processService.registerButton(scope, 'fetchButton');

                scope.$watch('disabled', function (newValue) {
                    template_btn.disabled = newValue;
                }, true);

                template_btn.onclick = function() {

                    var _init = function () {
                            scope.summaryData = {}; // reset
                            template_btn.disabled = true;
                            template_msg.innerHTML = 'Fetching data, please wait <span class="blink_me">_</span>';
                            processService.onFetching(true);
                        },

                        _getDataConstraints = function (biomarkers) {
                            if (typeof biomarkers !== 'undefined' && biomarkers.length > 0) {
                                var searchKeywordIds = biomarkers.map(function(biomarker) {
                                    return String(biomarker.id);
                                });
                                return dataConstraints = {
                                    search_keyword_ids: {
                                        keyword_ids: searchKeywordIds
                                    }
                                };
                            }
                        },

                        _fetchDataIntoRSession = function (conceptKeys, dataConstraints) {
                            return rServeService.loadDataIntoSession(conceptKeys, dataConstraints)
                                .then(function(msg) {
                                    return msg;
                                });
                        },

                        _finishedFetching = function (msg) {
                            template_msg.innerHTML = 'Success: ' + msg;
                            template_btn.disabled = false;
                            processService.onFetching(false);
                        },

                        _afterDataFetched = function (msg) {

                            if (!scope.showSummaryStats) {
                                return _finishedFetching(msg);
                            }

                            template_msg.innerHTML =
                                'Execute summary statistics, please wait <span class="blink_me">_</span>';

                            return rServeService.executeSummaryStats('fetch')
                                .then (function (data) {
                                    scope.summaryData = data.result;
                                    template_msg.innerHTML = 'Success: ' + data.msg;
                                }, function(msg) {
                                    template_msg.innerHTML = 'Failure: ' + msg;
                                })
                                .finally(function () {
                                    processService.onFetching(false);
                                    template_btn.disabled = false;
                                });
                        },
                        conceptKeys = smartRUtils.conceptBoxMapToConceptKeys(scope.conceptMap),
                        dataConstraints = _getDataConstraints(scope.biomarkers);

                    _init();

                    _fetchDataIntoRSession(conceptKeys, dataConstraints)
                        .then(_afterDataFetched);

                }; // end onclick
            }
        };
    }]);
