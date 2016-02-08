
smartRApp.controller('CorrelationController', ['$scope', 'rServeService', 'smartRUtils', function($scope, rServeService, smartRUtils) {

    rServeService.startSession('correlation');

    $scope.conceptBoxes = {
        datapoints: [],
        annotations: []
    };

    $scope.scriptResults = {};
    $scope.message = '';

    $scope.fetchData = function() {
        var promise = rServeService.loadDataIntoSession($scope.conceptBoxes);
        promise.done(function() {
            $scope.$apply(function() {
                $scope.message = 'Data loaded into R session!';
            });
        });

        promise.fail(function(error) {
            $scope.$apply(function() {
                $scope.message = error;
            });
        });
    };

    $scope.createViz = function() {
        rServeService.startScriptExecution({
            taskType: 'run',
            arguments: {}
        }).pipe(function(answer) {
            $scope.$apply(function() {
                $scope.scriptResults = JSON.parse(answer.result.artifacts.value);
            });
        })
    }
}]);
