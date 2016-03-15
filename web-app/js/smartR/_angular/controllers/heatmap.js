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
            summaryData : {}
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
            summaryData : {allSamples : 0}
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
            'fetch.conceptBoxes.highDimensional',           // input reference
            ['fetch.btn'],                                  // affected components
            commonWorkflowService.disableComponentsBasedOnInput
        );

        commonWorkflowService.registerCondition(
            'fetch.summaryData',
            ['preprocess.btn', 'runAnalysis.btn'],
            commonWorkflowService.disableComponentsBasedOnResults
        );


    }]);
