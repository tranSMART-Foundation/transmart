//# sourceURL=linegraph.js

'use strict';

window.smartRApp.controller('LinegraphController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('linegraph', $scope);

        $scope.fetch = {
            disabled: false,
            running: false,
            loaded: false,
            selectedBiomarkers: [],
            conceptBoxes: {
                highData: {concepts: [], valid: true},
                numData: {concepts: [], valid: true},
                catData: {concepts: [], valid: true}
            }
        };

        $scope.runAnalysis = {
            disabled: true,
            running: false,
            scriptResults: {},
            params: {}
        };

        $scope.$watchGroup(['fetch.running', 'runAnalysis.running'],
            function(newValues) {
                var fetchRunning = newValues[0],
                    runAnalysisRunning = newValues[1];

                // clear old results
                if (fetchRunning) {
                    $scope.runAnalysis.scriptResults = {};
                }

                // disable tabs when certain criteria are not met
                $scope.fetch.disabled = runAnalysisRunning;
                $scope.runAnalysis.disabled = fetchRunning || !$scope.fetch.loaded;
            }
        );

    }]);

