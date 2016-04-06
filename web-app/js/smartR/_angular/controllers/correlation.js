
window.smartRApp.controller('CorrelationController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('correlation', $scope);

        // model
        $scope.conceptBoxes = {
            datapoints: {concepts: [], valid: false},
            annotations: {concepts: [], valid: false}
        };
        $scope.scriptResults = {};
        $scope.params = {
            method: 'pearson'
        };
    }]);
