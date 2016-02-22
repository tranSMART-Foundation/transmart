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
            aggregateProbes : false
        };
        $scope.preprocessSummaryData = {summary:[]};

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        // TODO: Implement run analysis params inputs
        $scope.runArgs = {
            max_row : 100,
            sorting : 'nodes',
            ranking : 'coef'
        };
        $scope.scriptResults = {};
    }]);
