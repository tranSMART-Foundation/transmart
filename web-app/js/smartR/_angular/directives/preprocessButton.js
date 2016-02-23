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

                template_btn.disabled = true;
                template_msg.innerHTML = 'Calculating summary, please wait <span class="blink_me">_</span>';

                rServeService.preprocess(_args).then(
                    function(msg) {
                        if (_showSummary) {
                            rServeService.executeSummaryStats('fetch').then (
                                function(data) {
                                    scope.summaryData = data.result;
                                    template_msg.innerHTML = 'Success: ' + data.msg;
                                },
                                function(msg) {
                                    template_msg.innerHTML = 'Failure: ' + msg;
                                }
                            ).finally(function() {
                                template_btn.disabled = false;
                            });
                        } else {
                            template_msg.innerHTML = 'Success: ' + msg;
                            template_btn.disabled = false;
                        }
                    },
                    function(msg) {
                        template_msg.innerHTML = 'Failure: ' + msg;
                        template_btn.disabled = false;
                    }
                );
            };
        }
    };
}]);
