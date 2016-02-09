
smartRApp.controller('CorrelationController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        rServeService.startSession('correlation');

        // model
        $scope.conceptBoxes = {
            datapoints: [],
            annotations: []
        };
        $scope.scriptResults = {};
        $scope.message = '';

        $scope.fetchData = function() {
            var conceptKeys = smartRUtils.conceptBoxMapToConceptKeys($scope.conceptBoxes);

            rServeService.loadDataIntoSession(conceptKeys).then(
                function(msg) { $scope.message = 'Success: ' + msg; },
                function(msg) { $scope.message = 'Failure: ' + msg; }
            );
        };

        $scope.createViz = function() {
            rServeService.startScriptExecution({
                taskType: 'run',
                arguments: {}
            }).then(
                function(answer) { $scope.scriptResults = JSON.parse(answer.result.artifacts.value); },
                function(error) { $scope.message = error; }
            );
        }
    }]);
