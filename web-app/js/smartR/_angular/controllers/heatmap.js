
smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('heatmap');

        // model
        $scope.conceptBoxes = {
            highDimensional : [],
            numerical : [],
            categorical : []
        };

        $scope.scriptResults = {};
        $scope.params = {};

    }]);
