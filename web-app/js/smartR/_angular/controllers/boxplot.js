
window.smartRApp.controller('BoxplotController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('boxplot', $scope);

        // model
        $scope.conceptBoxes = {
            datapoints: [],
            subsets: []
        };
        $scope.scriptResults = {};
        $scope.params = {};
    }]);


