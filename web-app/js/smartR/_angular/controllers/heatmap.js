//# sourceURL=heatmap.js

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('heatmap', $scope);

        // ------------------------------------------------------------- //
        // Fetch data                                                    //
        // ------------------------------------------------------------- //
        $scope.fetch = {
            disabled : true,
            conceptBoxes : {
                highDimensional : [],
                numerical : [],
                categorical : []
            },
            selectedBiomarkers : [],
            summaryData : {}
        };

        // ------------------------------------------------------------- //
        // Preprocess                                                    //
        // ------------------------------------------------------------- //
        $scope.preprocess = {
            disabled : true,
            params :  {
                aggregateProbes : false
            },
            summaryData : {}
        };

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runAnalysis = {
            disabled : true,
            params: {
                max_row : 100,
                sorting : 'nodes',
                ranking : 'coef'
            },
            download : {
                disabled : true
            },
            subsets : 0,
            scriptResults : {}
        };
    }]);
