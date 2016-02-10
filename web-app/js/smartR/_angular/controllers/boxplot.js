
smartRApp.controller('BoxplotController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('boxplot');

        // model
        $scope.conceptBoxes = {};
        $scope.scriptResults = {};
        $scope.params = {};
    }]);
