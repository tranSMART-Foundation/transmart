//# sourceURL=preprocessButton.js

'use strict';

window.smartRApp.directive('preprocessButton',
    ['$rootScope', 'rServeService', function($rootScope, rServeService) {
        return {
            restrict: 'E',
            scope: {
                params: '=',
                showSummaryStats: '=',
                summaryData: '='
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/preprocessButton.html',
            link: function(scope, element) {

                var template_btn = element.children()[0];
                var template_msg = element.children()[1];

                template_btn.onclick = function() {
                    console.log('params', scope.params);
                    var _showSummary = scope.showSummaryStats,
                        _args = {aggregate:scope.params.aggregateProbes};

                    template_msg.innerHTML = 'Fetching data, please wait <span class="blink_me">_</span>';

                    rServeService.preprocess(_args).then(
                        function(msg) { template_msg.innerHTML = 'Success: ' + msg; },
                        function(msg) { template_msg.innerHTML = 'Failure: ' + msg; }
                    ).finally(function (){
                        if (_showSummary) {
                            rServeService.executeSummaryStats('preprocess').then (
                                function(data) {
                                    scope.summaryData = data.result;
                                    template_msg.innerHTML = 'Success: ' + data.msg;
                                },
                                function(msg) { template_msg.innerHTML = 'Failure: ' + msg; }
                            ).finally(function () {
                                template_btn.disabled = false;
                            });
                        }
                    });
                };
            }
        };
    }]);