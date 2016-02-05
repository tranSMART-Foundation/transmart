
smartRApp.controller('CorrelationController', ['$scope', 'rServeService', function($scope, rServeService) {

    rServeService.startSession('correlation');

    $scope.conceptBoxes = {
        datapoints: [],
        annotations: []
    };

    $scope.fetchData = function() {
        rServeService.loadDataIntoSession($scope.conceptBoxes);
    };

    $scope.createViz = function() {
        rServeService.startScriptExecution({
            taskType: 'run',
            arguments: {}
        }).pipe(function(answer) {
            var json = JSON.parse(answer.result.artifacts.value);
            SmartRCorrelation.create(json);
        })
    }
}]);
