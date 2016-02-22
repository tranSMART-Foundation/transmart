//# sourceURL=preprocessButton.js

window.smartRApp.directive('preprocessButton', ['rServeService', function(rServeService) {
    return {
        restrict: 'E',
        scope: {
            params: '=',
            showSummaryStats: '=',
            summaryData: '='
        },
        template:
            '<input type="button" value="Preprocess" class="heim-action-button">' +
            '<span style="padding-left: 10px;"></span>',
        link: function(scope, element) {

            var template_btn = element.children()[0];
            var template_msg = element.children()[1];

            template_btn.onclick = function() {
                var _showSummary = scope.showSummaryStats,
                    _args = {aggregate:scope.params.aggregateProbes};

                template_msg.innerHTML = 'Fetching data, please wait <span class="blink_me">_</span>';

                rServeService.preprocess(_args).then(
                    function(msg) { template_msg.innerHTML = 'Success: ' + msg; },
                    function(msg) { template_msg.innerHTML = 'Failure: ' + msg; }
                ).finally(function (){
                        if (_showSummary) {
                            template_msg.innerHTML = 'Execute summary, please wait <span class="blink_me">_</span>';
                            rServeService.executeSummaryStats('preprocess').then (
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
