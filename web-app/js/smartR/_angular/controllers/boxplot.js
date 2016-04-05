
window.smartRApp.controller('BoxplotController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('boxplot', $scope);

        // model
        $scope.conceptBoxes = {
            datapoints: {concepts: [], valid: false},
            subsets: {concepts: [], valid: false}
        };
        $scope.scriptResults = {};
        $scope.params = {};
    }]);


