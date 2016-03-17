//# sourceURL=heatmap.js

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'commonWorkflowService', function($scope, smartRUtils, commonWorkflowService) {

        commonWorkflowService.initializeWorkflow('heatmap', $scope);

        // ------------------------------------------------------------- //
        // Fetch data                                                    //
        // ------------------------------------------------------------- //
        $scope.fetch = {
            btn : {
                disabled : true
            },
            conceptBoxes : {
                highDimensional : [],
                numerical : [],
                categorical : []
            },
            selectedBiomarkers : [],
            scriptResults : {},
            totalSamples : 0,
            subsets : 0
        };

        // ------------------------------------------------------------- //
        // Preprocess                                                    //
        // ------------------------------------------------------------- //
        $scope.preprocess = {
            btn : {
                disabled : true
            },
            params :  {
                aggregateProbes : false
            },
            scriptResults : {}
        };

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runAnalysis = {
            btn : {
                disabled : true
            },
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

        commonWorkflowService.registerCondition(
            ['fetch.conceptBoxes.highDimensional'],
            ['fetch.btn'],
            commonWorkflowService.whenSelectHighDimensionalNodes
        );

        commonWorkflowService.registerCondition(
            ['fetch.btn', 'fetch.totalSamples', 'fetch.subsets'],
            ['preprocess', 'runAnalysis'],
            commonWorkflowService.whenFetchData
        );

    }]);
