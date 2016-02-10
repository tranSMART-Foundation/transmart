
smartRApp.controller('VolcanoplotController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('volcanoplot');

        // model
        $scope.conceptBoxes = {};
        $scope.scriptResults = {};
        $scope.params = {};
    }]);