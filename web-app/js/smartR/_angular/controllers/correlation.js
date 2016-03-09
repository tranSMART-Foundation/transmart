
window.smartRApp.controller('CorrelationController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('correlation', $scope);

        // model
        $scope.conceptBoxes = {
            datapoints: [],
            annotations: []
        };
        $scope.scriptResults = {};
        $scope.params = {
            method: 'pearson'
        };
    }]);
