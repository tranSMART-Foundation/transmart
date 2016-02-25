//# sourceURL=runButton.js

'use strict';

window.smartRApp.directive('runButton',
    ['$rootScope', 'rServeService', function($rootScope, rServeService) {
        return {
            restrict: 'E',
            scope: {
                storage: '=storeResultsIn',
                script: '@scriptToRun',
                name: '@buttonName',
                serialized: '@',
                arguments: '=argumentsToUse'
            },
            templateUrl: $rootScope.smartRPath +  '/js/smartR/_angular/templates/runButton.html',
            link: function(scope, element) {
                var template_btn = element.children()[0];
                var template_msg = element.children()[1];

                var serialized = JSON.parse(scope.serialized);

                var _successCreatePlot = function (response) {
                    template_msg.innerHTML = ''; // empty template
                    if (serialized) { // when results are serialized, we need to deserialized them by
                        // downloading the results files.
                        rServeService.downloadJsonFile(response.executionId, 'heatmap.json').then(
                            function (d) {
                                scope.storage = d.data;
                            }
                        );
                    } else { // results
                        scope.storage = JSON.parse(response.result.artifacts.value);
                    }
                };

                var _failCreatePlot = function (response) {
                    template_msg.style.color = 'red';
                    template_msg.innerHTML = 'Failure: ' + response.statusText;
                };

                template_btn.onclick = function() {
                    template_btn.disabled = true;
                    template_msg.style.color = 'black';
                    template_msg.innerHTML = 'Creating plot, please wait <span class="blink_me">_</span>';
                    rServeService.startScriptExecution({
                        taskType: scope.script,
                        arguments: scope.arguments
                    }).then(
                        _successCreatePlot,
                        _failCreatePlot
                    ).finally(function() {
                        template_btn.disabled = false;
                    });
                };
            }
        };
    }]);
