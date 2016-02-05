
smartRApp.controller('CorrelationController', ['$scope', 'rServeService', function($scope, rServeService) {

    rServeService.startSession('correlation');

    $scope.conceptBoxes = {
        datapoints: [],
        annotations: []
    };

    $scope.fetchData = function() {
        rServeService.loadDataIntoSession($scope.conceptBoxes);
    };
}]);
