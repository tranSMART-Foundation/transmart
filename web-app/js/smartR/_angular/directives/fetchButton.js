//# sourceURL=fetchButton.js

window.smartRApp.directive('fetchButton', ['rServeService', 'smartRUtils', function(rServeService, smartRUtils) {
    return {
        restrict: 'E',
        scope: {
            conceptMap: '=',
            biomarkers: '=',
            showSummaryStats: '=',
            summaryData: '='
        },
        template:
            '<input type="button" value="Fetch Data" class="heim-action-button">' +
            '<span style="padding-left: 10px;"></span>',
        link: function(scope, element) {

            var template_btn = element.children()[0];
            var template_msg = element.children()[1];

            template_btn.onclick = function() {

                var showSummary = scope.showSummaryStats;

                template_btn.disabled = true;
                template_msg.innerHTML = 'Fetching data, please wait <span class="blink_me">_</span>';

                // Construct query constraints
                var conceptKeys = smartRUtils.conceptBoxMapToConceptKeys(scope.conceptMap);
                var dataConstraints;

                if (typeof scope.biomarkers !== 'undefined' && scope.biomarkers.length > 0) {
                    var searchKeywordIds = scope.biomarkers.map(function(biomarker) {
                        return biomarker.id;
                    });
                    dataConstraints = {
                        search_keyword_ids: {
                            keyword_ids: searchKeywordIds
                        }
                    };
                }

                rServeService.loadDataIntoSession(conceptKeys, dataConstraints).then(
                    function(msg) { template_msg.innerHTML = 'Success: ' + msg; },
                    function(msg) { template_msg.innerHTML = 'Failure: ' + msg; }
                ).finally(function() {
                    if (showSummary) {
                        template_msg.innerHTML = 'Execute summary, please wait <span class="blink_me">_</span>';
                        rServeService.executeSummaryStats('fetch').then (
                            function(data) {
                                scope.summaryData = data.result;
                                template_msg.innerHTML = 'Success: ' + data.msg;
                            },
                            function(msg) { template_msg.innerHTML = 'Failure: ' + msg; }
                        ).finally(function () {
                                template_btn.disabled = false;
                            });
                    } else {
                        template_btn.disabled = false;
                    }
                });

            };
        }
    };
}]);
