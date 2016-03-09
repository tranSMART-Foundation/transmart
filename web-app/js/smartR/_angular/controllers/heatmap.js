//# sourceURL=heatmap.js

window.smartRApp.controller('HeatmapController',
    ['$scope', 'smartRUtils', 'rServeService', '$css', function($scope, smartRUtils, rServeService, $css) {

        // load workflow specific css
        $css.bind({
            href: $scope.smartRPath + '/css/heatmap.css'
        }, $scope);

        // initialize service
        rServeService.startSession('heatmap');

        // ------------------------------------------------------------- //
        // Fetch data                                                    //
        // ------------------------------------------------------------- //
        $scope.fetch = {
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
            params :  {
                aggregateProbes : false
            },
            summaryData : {}
        };

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runAnalysis = {
            params: {
                max_row : 100,
                sorting : 'nodes',
                ranking : 'coef'
            },
            scriptResults : {}
        };
    }]);
