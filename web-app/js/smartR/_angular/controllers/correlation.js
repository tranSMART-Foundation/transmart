
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
            var promise = rServeService.loadDataIntoSession(conceptKeys);

            promise.then(function(msg) {
                $scope.message = 'Success: ' + msg;
            }, function(msg) {
                $scope.message = 'Failure: ' + msg;
            });
        };

        $scope.createViz = function() {
            var promise = rServeService.startScriptExecution({
                taskType: 'run',
                arguments: {}
            });

            promise.done(function(answer) {
                $scope.scriptResults = JSON.parse(answer.result.artifacts.value);
            });

            promise.fail(function(error) {
                $scope.message = error;
            })
        }
    }]);
