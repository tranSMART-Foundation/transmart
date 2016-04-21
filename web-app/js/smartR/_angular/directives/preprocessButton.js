//# sourceURL=preprocessButton.js

'use strict';

window.smartRApp.directive('preprocessButton', [
    'rServeService',
    '$rootScope',
    function(rServeService, $rootScope) {
        return {
            restrict: 'E',
            scope: {
                running: '=?',
                params: '=?',
                showSummaryStats: '=',
                summaryData: '='
            },
            templateUrl: $rootScope.smartRPath + '/js/smartR/_angular/templates/preprocessButton.html',
            link: function(scope, element) {

                var template_btn = element.children()[0];
                var template_msg = element.children()[1];

                var _onSuccess = function() {
                    template_msg.innerHTML = 'Task complete!';
                    template_btn.disabled = false;
                    scope.running = false;
                };

                var _onFail = function(msg) {
                    template_msg.innerHTML = 'Error: ' + msg;
                    template_btn.disabled = false;
                    scope.running = false;
                };

                var _showSummaryStats = function() {
                    template_msg.innerHTML = 'Execute summary statistics, please wait <span class="blink_me">_</span>';
                    rServeService.executeSummaryStats('preprocess').then(
                        function (data) {
                            scope.summaryData = data.result;
                            _onSuccess();
                        },
                        _onFail
                    );
                };

                template_btn.onclick = function() {
                    scope.summaryData = {};
                    scope.disabled = true;
                    scope.running = true;
                    template_msg.innerHTML = 'Preprocessing, please wait <span class="blink_me">_</span>';

                    var params = scope.params ? scope.params : {};
                    rServeService.preprocess(params).then(
                        scope.showSummaryStats ? _showSummaryStats : _onSuccess,
                        _onFail
                    );
                };
            }
        };
    }]);
