var app = angular.module('smartR', []);

app.controller('CorrelationController', ['$scope', 'rServeService', function($scope, rServeService) {
    $scope.conceptBoxes = [];
    $scope.workflowData = {};
    $scope.fetchData = function() { rServeService.fetchData(); }; // FIXME: mock-up
    $scope.runWorkflow = function() { $scope.workflowData = rServeService.runWorkflow(); }; // FIXME: mock-up
    window.SmartRCorrelation.create(JSON.parse($scope.workflowData));
}]);
