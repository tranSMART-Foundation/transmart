
'use strict';

window.smartRApp.controller('BoxplotController', [
    '$scope',
    'smartRUtils',
    'commonWorkflowService',
    function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('boxplot', $scope);

        // model
        $scope.conceptBoxes = {
            datapoints: []
        };
        $scope.scriptResults = {};
        $scope.params = {};
    }]);


