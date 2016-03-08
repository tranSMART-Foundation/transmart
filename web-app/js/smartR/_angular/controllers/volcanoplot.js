//# sourceURL=volcanoplot.js

'use strict';

window.smartRApp.controller('VolcanoplotController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('volcanoplot', $scope);

        // model
        $scope.conceptBoxes = {
            highDimensional: []
        };
        $scope.scriptResults = {};
        $scope.params = {};
    }]);
