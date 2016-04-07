//# sourceURL=runButton.js

'use strict';

window.smartRApp.directive('runButton',
    ['$rootScope', 'rServeService', function($rootScope, rServeService) {
        return {
            restrict: 'E',
            scope: {
                running: '=',
                storage: '=storeResultsIn',
                script: '@scriptToRun',
                name: '@buttonName',
                serialized: '=',
                arguments: '=argumentsToUse'
            },
            templateUrl: $rootScope.smartRPath + '/js/smartR/_angular/templates/runButton.html',
            link: function(scope, element) {

                var template_btn = element.children()[0],
                    template_msg = element.children()[1],
                    serialized = scope.serialized;

                var _downloadData = function(response) {
                    template_msg.innerHTML = ''; // empty template
                    if (serialized) { // when results are serialized, we need to deserialized them by
                        // downloading the results files.
                        rServeService.downloadJsonFile(response.executionId, 'heatmap.json').then(
                            function(d) { scope.storage = d.data; _done(); },
                            _onFail
                        );
                    } else { // results
                        scope.storage = JSON.parse(response.result.artifacts.value);
                        _done();
                    }
                };

                var _done = function() {
                    template_btn.disabled = false;
                    scope.disabled = false;
                    scope.running = false;
                };

                var _onFail = function(response) {
                    template_msg.style.color = 'red';
                    template_msg.innerHTML = 'Failure: ' + response.statusText;
                    _done();
                };

                template_btn.onclick = function() {
                    template_btn.disabled = true;
                    scope.storage = {};
                    scope.disabled = true;
                    scope.running = true;
                    template_msg.innerHTML = 'Creating plot, please wait <span class="blink_me">_</span>';

                    rServeService.startScriptExecution({
                        taskType: scope.script,
                        arguments: scope.arguments
                    }).then(
                        _downloadData,
                        _onFail
                    );
                };
            }
        };
    }]);
