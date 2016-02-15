
window.smartRApp.controller('CorrelationController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('correlation');

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
