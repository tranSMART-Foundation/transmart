
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

            promise.always(function(msg) {
                $scope.message = msg;
                $scope.$apply();
            });
        };

        $scope.createViz = function() {
            var promise = rServeService.startScriptExecution({
                taskType: 'run',
                arguments: {}
            });

            promise.done(function(answer) {
                $scope.scriptResults = JSON.parse(answer.result.artifacts.value);
                $scope.$apply();
            });

            promise.fail(function(error) {
                $scope.message = error;
                $scope.$apply();
            })
        }
    }]);
