
smartRApp.controller('CorrelationController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        rServeService.startSession('correlation');

        // model
        $scope.conceptBoxes = {
            datapoints: [],
            annotations: []
        };
        $scope.scriptResults = {};

        $scope.createViz = function() {
            rServeService.startScriptExecution({
                taskType: 'run',
                arguments: {}
            }).then(
                function(msg) { $scope.scriptResults = JSON.parse(msg.result.artifacts.value); },
                function(msg) { $scope.message = msg; }
            );
        }
    }]);
