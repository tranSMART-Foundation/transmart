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
            disabled : false,
            selectedBiomarkers : [],
            summaryData : {}
        };

        $scope.$on('on:fetching', function (event, disabled) {
            $scope.preprocess.disabled = disabled;
            $scope.runAnalysis.disabled = disabled;
            $scope.runAnalysis.download.disabled = true;
            // empty previous results
            $scope.preprocess.summaryData = {};
            $scope.runAnalysis.scriptResults = {};
        });

        // ------------------------------------------------------------- //
        // Preprocess                                                    //
        // ------------------------------------------------------------- //
        $scope.preprocess = {
            params :  {
                aggregateProbes : false
            },
            disabled : true,
            summaryData : {}
        };

        $scope.$on('on:preprocessing', function (event, disabled) {
            $scope.fetch.disabled = disabled;
            $scope.runAnalysis.disabled = disabled;
            $scope.runAnalysis.download.disabled = true;
            $scope.runAnalysis.scriptResults = {};
        });

        // ------------------------------------------------------------- //
        // Run Heatmap                                                   //
        // ------------------------------------------------------------- //
        $scope.runAnalysis = {
            params: {
                max_row : 100,
                sorting : 'nodes',
                ranking : 'coef'
            },
            disabled : true,
            download :  {disabled : true},
            scriptResults : {}
        };

        $scope.$on('on:running', function (event, disabled) {
            $scope.preprocess.disabled = disabled;
            $scope.fetch.disabled = disabled;
            $scope.runAnalysis.download.disabled = disabled;
        });

    }]);
