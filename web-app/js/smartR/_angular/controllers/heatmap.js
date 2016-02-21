//# sourceURL=heatmap.js

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'rServeService', function($scope, smartRUtils, rServeService) {

        // initialize service
        rServeService.startSession('heatmap');

        // ------------------------------------------------------------- //
        // Fetch data                                                    //
        // ------------------------------------------------------------- //
        $scope.conceptBoxes = {
            highDimensional : [],
            numerical : [],
            categorical : []
        };
        $scope.selectedBiomarkers = [];
        $scope.fetchSummaryData = {summary:[]};

        // ------------------------------------------------------------- //
        // Preprocess                                                    //
        // ------------------------------------------------------------- //
        $scope.preprocessArgs = {
            aggregateProbes : true
        };
        $scope.preprocessSummaryData = {summary:[]};

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runArgs = {
            maxRow : 100,
            sortOn : 'nodes',
            rankCriteria : 'coef'
        };
        $scope.scriptResults = {};

    }]);
